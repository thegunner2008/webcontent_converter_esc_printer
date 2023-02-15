

## webcontent_converter_for_esc_printer

This package is a converter image for ESC/POS printer, to PDF from html, url.
Support multiple platform (Android, iOS, Web).

## Usage


- contentToImage({ String html, double duration, String? executablePath, int scale, int width, }): convert from html with fixed width. This function will return Uint8List Image

- webUriToImage(String uri, double duration) : uri is web uri(url) and duration is delay time. This function will return Uint8List Image


```dart
displayImage:  
  FutureBuilder(
        future: WebcontentConverter.contentToImage(
          content: _htmlText,
          executablePath: WebViewHelper.executablePath(),
          width: 558,
        ),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            return Image.memory(snapshot.data as Uint8List);
          }
          return const SizedBox();
        }),
  )

printImage:
    final ipAddress = '<YOUR_PRINTER_IP>'
    final _html ='<YOUR_HTML_CONTENT>'
    final executablePath = WebViewHelper.isChromeAvailable ? WebViewHelper.executablePath() : null;
    var bytes = await WebcontentConverter.contentToImage(
        content: _html,
        executablePath: executablePath,
        width: 558,
    );
    
    final printer = NetworkPrinter(PaperSize.mm80, await CapabilityProfile.load());
    final connectPrinter = await printer.connect(ipAddress, port: 9100);

    if (bytes.isNotEmpty && connectPrinter == PosPrintResult.success) {
        final img = print_img.decodeImage(bytes);
        printer.image(img!);
        printer.feed(1);
        printer.disconnect();
    }
```

## Additional information

