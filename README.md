## Readium 2 Sandbox

### Using The R2 API

#### Obtain a `Publication` and a `Container`

In the Readium 2 API, a `Publication` is effectively an in-memory
representation of a [Readium Webpub manifest](https://readium.org/webpub-manifest/).
A `Container` is effectively a reference to a container of content, such
as a physical EPUB file that contains book chapters and images.

The `EpubParser` class has methods that, given a filename, can produce
both a `Publication` and a `Container`.

```
  val box =
    EpubParser().parse(file.absolutePath)
      ?: throw IOException("Failed to parse EPUB")
```

This step is performed in the [create method of the ReaderServer class](./app/src/main/java/org/librarysimplified/r2_sandbox/app/ReaderServer.kt#L77).

Unfortunately, the `EpubParser` class fails to provide any kind of
diagnostic messages or information to explain why parsing may have
failed.

#### Start An Embedded HTTP Server

The Readium 2 API assumes that content from an EPUB file will be served
to platform `WebView` via an embedded web server ([NanoHTTPD](https://github.com/NanoHttpd/nanohttpd/).

The [Server](https://github.com/readium/r2-streamer-kotlin/blob/develop/r2-streamer/src/main/java/org/readium/r2/streamer/server/Server.kt)
class included as part of the Readium 2 Streamer module must be started:

```
  val server = Server(port)
  this.logger.debug("starting server")
  server.start(5_000)

  this.logger.debug("loading readium resources")
  server.loadReadiumCSSResources(context.assets)
  server.loadR2ScriptResources(context.assets)
  server.loadR2FontResources(context.assets, context)
```

The `Server` class calls the Readium 2 [Fetcher](https://github.com/readium/r2-streamer-kotlin/blob/develop/r2-streamer/src/main/java/org/readium/r2/streamer/fetcher/Fetcher.kt)
internally, which instantiates a non-configurable list of
[ContentFilter](https://github.com/readium/r2-streamer-kotlin/blob/develop/r2-streamer/src/main/java/org/readium/r2/streamer/fetcher/ContentFilter.kt)
implementations based on the detected content type of the `Container`.
One of the content filter implementations it adds is the
[ContentFilterEpub](https://github.com/readium/r2-streamer-kotlin/blob/develop/r2-streamer/src/main/java/org/readium/r2/streamer/fetcher/ContentFilter.kt#L30)
class. The `ContentFilterEpub` class unconditionally injects references 
to JavaScript and CSS files into every XHTML file loaded from the EPUB
file. These JavaScript and CSS files are expected to be provided by
the Readium 2 [Navigator](https://github.com/readium/r2-navigator-kotlin)
implementation. This sets up an unfortunate circular dependency; the
`Streamer` depends on the `Navigator` to provide resources, and the
`Navigator` expects the `Streamer` to inject references to those resources.
This tightly couples the two modules together and means that, as we don't
want to use the `Navigator`, we have to provide those resources ourselves.
The set of resources required is undocumented and may change at any time
with any new version of Readium.

We then load the EPUB file into the server:

```
  this.logger.debug("loading epub into server")
  val epubName = "/${file.name}"
  server.addEpub(
    publication = box.publication,
    container = box.container,
    fileName = epubName,
    userPropertiesPath = null
  )
```

Note that if `epubName` does not begin with a slash, Readium will
crash with an exception due to constructing an invalid URL internally:

```
java.net.MalformedURLException: For input string: "8080tmp4745233484591844051.epub"
	at java.net.URL.<init>(URL.java:627)
	at java.net.URL.<init>(URL.java:490)
	at java.net.URL.<init>(URL.java:439)
	at org.readium.r2.shared.Publication.addSelfLink(Publication.kt:217)
	at org.readium.r2.streamer.server.AbstractServer.addEpub(Server.kt:179)
```

#### Construct Chapter URLs

In order to retrieve content from the EPUB file for rendering, we need
to construct a URL that points to one of the items in the `readingOrder`
list in the `Publication` value we produced earlier. The `readingOrder`
list effectively acts as a table of contents, and the items in the list
are specified in reading order (unsurprisingly).

For a given example resource `/x/y/z.xhtml`, the embedded `Server` will
make that resource available at an endpoint prefixed with the `fileName`
value that we passed to the `addEpub()` method in the previous step.

As an example, if we passed in a `fileName` of `/f.epub`, and the
server is configured to listen on port `8080`, we can get access to
the contents of `/x/y/z.xhtml` by requesting the following URI:

```
http://127.0.0.1:8080/f.epub/x/y/z.xhtml
```

If we fail to include the `f.epub` prefix, an internal `NullPointerException`
will be logged internally due to a misconfiguration of the internal
NanoHTTPD server:

```
2020-01-17 12:00:17.875 22591-22854/org.librarysimplified.r2_sandbox.app E/NanoHTTPD: Communication with the client broken, or an bug in the handler code
    java.lang.NullPointerException: Attempt to invoke virtual method 'org.nanohttpd.protocols.http.response.Response org.nanohttpd.router.RouterNanoHTTPD$UriResource.process(java.util.Map, org.nanohttpd.protocols.http.IHTTPSession)' on a null object reference
        at org.nanohttpd.router.RouterNanoHTTPD$UriRouter.process(RouterNanoHTTPD.java:597)
        at org.nanohttpd.router.RouterNanoHTTPD.serve(RouterNanoHTTPD.java:672)
        at org.nanohttpd.protocols.http.NanoHTTPD$1.handle(NanoHTTPD.java:376)
        at org.nanohttpd.protocols.http.NanoHTTPD$1.handle(NanoHTTPD.java:372)
        at org.nanohttpd.protocols.http.NanoHTTPD.handle(NanoHTTPD.java:535)
        at org.nanohttpd.protocols.http.HTTPSession.execute(HTTPSession.java:418)
        at org.nanohttpd.protocols.http.ClientHandler.run(ClientHandler.java:75)
        at java.lang.Thread.run(Thread.java:764)
```

You should expect to see lots of these exceptions in normal usage.

Each `readingOrder` item is a link to a resource, and the `href` field
contains the (absolute) internal path of the resource. We can therefore
construct a URL for any `readingOrder` item using the following method:

```
  override fun locationOfSpineItem(index: Int): String {
    require(index < this.publication.readingOrder.size) { 
      "Only indices in the range [0, ${this.publication.readingOrder.size}) are valid" 
    }
    
    return buildString {
      this.append("http://127.0.0.1:")
      this.append(this@ReaderServer.port)
      this.append(this@ReaderServer.epubFileName)

      val publication = this@ReaderServer.publication
      this.append(publication.readingOrder[index].href 
        ?: throw IllegalStateException("Link to chapter ${index} is not present"))
    }
  }
```

#### Create A WebView

We need to create a standard platform `WebView`, and the `WebView`
must be configured to allow the execution of JavaScript.

This is performed in the `onCreateView` method of the [ReaderViewerFragment](./app/src/main/java/org/librarysimplified/r2_sandbox/app/ReaderViewerFragment.kt)
class.

As mentioned earlier, the Readium 2 `Streamer` implementation
unconditionally injects references to JavaScript files into any loaded
XHTML file. This means that, at the time of writing, we must ensure
that several JavaScript files are available in the Android assets of
the application. Specifically, the files `utils.js` and `touchHandling.js`
must be present in a `scripts` subdirectory of the `assets` directory.

#### Register JavaScript API receivers

The `utils.js` and `touchHandling.js` files included in the Readium
2 `Navigator` module contain code to handle touch events made to the
`WebView`, and to handle scrolling to specific "pages" within a given
chapter. Pagination is achieved by using custom CSS resources (injected
by the `Streamer`) to divide the text of a chapter into screen-sized
chunks. The code inside `utils.js` simply divides the total height of
the document by the height of the screen, and allows for instantly
scrolling forwards and backwards by one screen-sized amount at a time.
This effectively provides the illusion of moving forwards and backwards
by one page of text at a time.

The code in both files expects to be able to make calls back to Kotlin
code by calling functions on a global JavaScript object named `Android`.
It's therefore necessary to register an instance of a class with the
`WebView` that contains methods that match the signatures of those 
expected by the JavaScript code. Those method signatures are undocumented
and are subject to change at any time with any new version of Readium.

At the time of writing, those methods have been determined to match
those declared in the [ReaderJavascriptAPIReceiverType](./app/src/main/java/org/librarysimplified/r2_sandbox/app/ReaderJavascriptAPIReceiverType.kt)
interface. We provide a single implementation of this class that
simply logs calls made from the JavaScript code.

The `utils.js` class also provides JavaScript functions that are
expected to be called from Kotlin (by way of evaluating those functions
on the `WebView`). Again, these functions are undocumented and are
subject to change at any time. The [ReaderJavascriptAPIType](./app/src/main/java/org/librarysimplified/r2_sandbox/app/ReaderJavascriptAPIType.kt)
interface describes the two methods we care about, and a single
implementation of this class is provided that performs the actual
JavaScript function calls.

#### Load A URL And Watch What Happens

Performing all of the previous steps can take some time, and so should
be performed on a background thread. In our implementation, we do all
of this work on a standard Java `Executor` instance, and we register
a callback to be called when all of the work is completed. The registration
occurs in the `onStart` method of the [ReaderViewerFragment](./app/src/main/java/org/librarysimplified/r2_sandbox/app/ReaderViewerFragment.kt)
class.

```
    /*
     * Load the book asynchronously, and evaluate the given functions when the book either
     * loads, or fails to load.
     */

    val exec = MoreExecutors.directExecutor()
    FluentFuture.from(this.readerModel.openBook(activity, bookFile))
      .transform(Function<ReaderServerType, Unit> { server ->
        this.bookIsReady(server!!)
      }, exec)
      .catching(Exception::class.java, Function<java.lang.Exception, Unit> { exception ->
        this.bookFailedToOpen(exception!!)
      }, exec)
```

If loading a book fails, we display an error and pop the fragment
from the stack. If loading the book succeeds, we find the URL of the
first item in the `Publication` and open it in the `WebView`:

```
  @UiThread
  private fun bookIsReadyUI(server: ReaderServerType) {
    val startingLocation = server.startingLocation()
    this.logger.debug("opening starting location: {}", startingLocation)
    this.webView.loadUrl(startingLocation)

    this.filePrevious.setOnClickListener {
      this.webView.loadUrl(server.locationOfSpineItem(this.readerModel.findPreviousChapterIndex()))
    }
    this.fileNext.setOnClickListener {
      this.webView.loadUrl(server.locationOfSpineItem(this.readerModel.findNextChapterIndex()))
    }
    this.pagePrevious.setOnClickListener {
      this.jsAPI.scrollPrevious()
    }
    this.pageNext.setOnClickListener {
      this.jsAPI.scrollNext()
    }
  }
```

For the sake of this example code, we provide four buttons onscreen.
Two buttons allow moving forwards and backwards across chapters, and
the other two buttons allow moving forwards and backwards by a page
at a time. Note that if we reach the end of a chapter, the "next page"
button will _not_ take us to the next chapter; this has to be handled
by the hosting Kotlin code.

#### Handling Chapter Changes

The functions provided in the `utils.js` file allow for scrolling
forwards or backwards by pages. Normally, these functions return an
empty string as a result. However, upon reaching the end (or start) of
a chapter, these functions will instead return the constant string `"edge"`.

A real application using the Readium 2 JavaScript functions must
therefore inspect the return values of the JavaScript functions in order
to determine that it's time to fetch a new URL from the `Publication`
so that a new chapter can be loaded into the `WebView`. For the sake
of brevity, this is not implemented in this example application.

The code in `utils.js` also makes calls to an `onChapterProgressionChanged`
method that is expected to be defined in Kotlin (in our registered
`org.librarysimplified.r2_sandbox.app.ReaderJavascriptAPIReceiverType`
type, specifically). The method receives a string representation of
the current chapter progress, as a real value in the range `[0, 1]`.

#### Handling Screen Taps

When the user taps in the middle of the web view, the JavaScript
code provided in the `touchHandling.js` file will call a method
named `onCenterTapped` that must be defined in Kotlin (in our registered
`org.librarysimplified.r2_sandbox.app.ReaderJavascriptAPIReceiverType`
type, specifically). Additionally, the code will also call `onLeftTapped` 
and `onRightTapped` methods to indicate that the user tapped on the 
edges of the screen. A real application would respond to these methods 
by hiding/showing UI controls, and/or loading new chapters as necessary. 
Our example application simply logs the calls and does nothing.
