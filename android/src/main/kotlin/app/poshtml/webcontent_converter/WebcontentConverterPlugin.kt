package app.poshtml.webcontent_converter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Picture
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.print.PdfPrinter
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/** WebcontentConverterPlugin */
class WebcontentConverterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var webView: WebView

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val viewID = "webview-view-type"
        flutterPluginBinding.platformViewRegistry.registerViewFactory(viewID, FLNativeViewFactory())
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "webcontent_converter")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        val method = call.method
        val arguments = call.arguments as Map<*, *>
        val content = arguments["content"] as String
        val duration = arguments["duration"] as Double?
        val savedPath = arguments["savedPath"] as? String
        val margins = arguments["margins"] as Map<String, Double>?
        val format = arguments["format"] as Map<String, Double>?
        val width = arguments["width"] as Int?

        when (method) {
            "contentToImage" -> {
                print("\n activity $activity")
                webView = WebView(this.context)
                val dwidth = this.activity.window.windowManager.defaultDisplay.width
                val dheight = this.activity.window.windowManager.defaultDisplay.height
                webView.layout(0, 0, dwidth, dheight)
                webView.loadDataWithBaseURL(null, content, "text/HTML", "UTF-8", null)
                val scale =
                    (this.context.resources.displayMetrics.widthPixels / this.context.resources.displayMetrics.density).toInt()
                webView.setInitialScale(scale)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)

                        Handler(Looper.getMainLooper()).postDelayed({
                            val picture = webView.capturePicture()
                            if (picture.width > 0 && picture.height > 0) {
                                val data = picture.toBitmap(width ?: 0);
                                if (data != null) {
                                    val bytes = data.toByteArray()
                                    result.success(bytes)
                                    return@postDelayed
                                }
                            }
                            result.error("UNAVAILABLE", "Have no data", null)
                        }, 300)
                    }
                }
            }
            "contentToPDF" -> {
                print("\n activity $activity")
                webView = WebView(this.context)
                val dwidth = this.activity.window.windowManager.defaultDisplay.width
                val dheight = this.activity.window.windowManager.defaultDisplay.height
                webView.layout(0, 0, dwidth, dheight)
                webView.loadDataWithBaseURL(null, content, "text/HTML", "UTF-8", null)
                webView.setInitialScale(1)
                webView.settings.javaScriptEnabled = true
                webView.settings.useWideViewPort = true
                webView.settings.javaScriptCanOpenWindowsAutomatically = true
                webView.settings.loadWithOverviewMode = true
                print("\n=======> enabled scrolled <=========")
                WebView.enableSlowWholeDocumentDraw()

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)

                        Handler().postDelayed({
                            print("\nOS Version: ${android.os.Build.VERSION.SDK_INT}")
                            print("\n ================ webview completed ==============")
                            print("\n scroll delayed ${webView.scrollBarFadeDuration}")

                            webView.exportAsPdfFromWebView(
                                savedPath!!,
                                format!!,
                                margins!!,
                                object : PdfPrinter.Callback {
                                    override fun onSuccess(filePath: String) {
                                        result.success(filePath)
                                    }

                                    override fun onFailure() {
                                        result.success(null)
                                    }
                                })

                        }, duration!!.toLong())

                    }
                }

            }
            "printPreview" -> {
                print("\n activity $activity")
                webView = WebView(this.context)
                val dwidth = this.activity.window.windowManager.defaultDisplay.width
                val dheight = this.activity.window.windowManager.defaultDisplay.height
                webView.layout(0, 0, dwidth, dheight)
                webView.loadDataWithBaseURL(null, content, "text/HTML", "UTF-8", null)
                webView.setInitialScale(1)
                webView.settings.javaScriptEnabled = true
                webView.settings.useWideViewPort = true
                webView.settings.javaScriptCanOpenWindowsAutomatically = true
                webView.settings.loadWithOverviewMode = true
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    print("\n=======> enabled scrolled <=========")
                    WebView.enableSlowWholeDocumentDraw()
                }

                print("\n ///////////////// webview setted /////////////////")

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)

                        Handler().postDelayed({
                            print("\nOS Version: ${android.os.Build.VERSION.SDK_INT}")
                            print("\n ================ webview completed ==============")
                            print("\n scroll delayed ${webView.scrollBarFadeDuration}")

                            createWebPrintJob(webView);

                        }, duration!!.toLong())

                    }
                }

            }
            else
            -> result.notImplemented()
        }
    }

    //test to save bitmap to file
    fun saveWebView(data: Bitmap): Boolean {
        var path = this.context.getExternalFilesDir(null).toString() + "/sample.jpg"
        var file = File(path)
        file.writeBitmap(data!!, Bitmap.CompressFormat.JPEG, 100)
        return true
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        print("onAttachedToActivity")
        activity = binding.activity
        webView = WebView(activity.applicationContext)
        webView.minimumHeight = 1
        webView.minimumWidth = 1
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // TODO: the Activity your plugin was attached to was destroyed to change configuration.
        // This call will be followed by onReattachedToActivityForConfigChanges().
        print("onDetachedFromActivityForConfigChanges");
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        // TODO: your plugin is now attached to a new Activity after a configuration change.
        print("onAttachedToActivity")
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        // TODO: your plugin is no longer associated with an Activity. Clean up references.
        print("onDetachedFromActivity")
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun createWebPrintJob(webView: WebView) {

        // Get a PrintManager instance
        (activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
            val applicationName = activity.applicationContext.applicationInfo.name;
            val jobName = "$applicationName print preview"

            // Get a print adapter instance
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            val printAttributes =
                PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build();
            // Create a print job with name and adapter instance
            printManager.print(
                jobName,
                printAdapter,
                printAttributes
            )
        }
    }
}

fun WebView.exportAsPdfFromWebView(
    savedPath: String,
    format: Map<String, Double>,
    margins: Map<String, Double>,
    callback: PdfPrinter.Callback
) {
    print("\nsavedPath ${savedPath}")
    val attributes = PrintAttributes.Builder()
        .setMediaSize(
            PrintAttributes.MediaSize(
                "${format["width"]}-${format["height"]}",
                "android",
                format["width"]!!.convertFromInchesToInt(),
                format["height"]!!.convertFromInchesToInt()
            )
        )
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
        .setMinMargins(
            PrintAttributes.Margins(
                margins["left"]!!.convertFromInchesToInt(),
                margins["top"]!!.convertFromInchesToInt(),
                margins["right"]!!.convertFromInchesToInt(),
                margins["bottom"]!!.convertFromInchesToInt()
            )
        )
        .build()
    val file = File(savedPath)
    val fileName = file.absoluteFile.name
    val pdfPrinter = PdfPrinter(attributes)
    val adapter = this.createPrintDocumentAdapter(fileName)
    pdfPrinter.print(adapter, file, callback)
}

fun Double.convertFromInchesToInt(): Int {
    if (this > 0) {
        return (this.toInt() * 1000)
    }
    return this.toInt()
}

fun Picture.toBitmap(widthScale: Int): Bitmap? {
    val bmp = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(bmp, 0F, 0F, null)
    this.draw(canvas)

    val heightScale = this.height * widthScale / this.width
    return Bitmap.createScaledBitmap(bmp, widthScale, heightScale, true)
}

fun Bitmap.toByteArray(): ByteArray {
    ByteArrayOutputStream().apply {
        compress(Bitmap.CompressFormat.PNG, 100, this)
        return toByteArray()
    }
}

fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    try {
        val fout = FileOutputStream(this.path)
        bitmap.compress(format, quality, fout)
        fout.flush()
        fout.close()
    } catch (e: Exception) {
        e.printStackTrace();
    }
}