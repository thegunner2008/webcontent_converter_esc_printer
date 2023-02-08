import 'package:esc_pos_printer/esc_pos_printer.dart';
import 'package:esc_pos_utils/esc_pos_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:image/image.dart' as print_img;
import 'package:webcontent_converter_esc_printer/demo.dart';
import 'package:webcontent_converter_esc_printer/webcontent_converter_esc_printer.dart';


class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  String _htmlText = Demo.getShortReceiptContent();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Print from HTML'),
      ),
      body: Form(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: <Widget>[
              TextFormField(
                decoration: const InputDecoration(labelText: 'HTML Text'),
                keyboardType: TextInputType.multiline,
                maxLines: null,
                validator: (value) {
                  if (value?.isNotEmpty ?? false) {
                    return 'Please enter some HTML text';
                  }
                  return null;
                },
                onSaved: (value) => _htmlText = value ?? "",
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  ElevatedButton(
                    child: const Text('Print from HTML'),
                    onPressed: () async {
                      var bytes = await WebcontentConverter.contentToImage(
                        content: _htmlText,
                        executablePath: WebViewHelper.executablePath(),
                        scale: 3,
                        width: 558,
                      );

                      final printer =
                      NetworkPrinter(PaperSize.mm80, await CapabilityProfile.load());
                      final connectPrinter = await printer.connect('172.20.177.250', port: 9100);

                      if (bytes.isNotEmpty) {
                        final img = print_img.decodeImage(bytes);
                        printer.imageRaster(img!);
                        printer.feed(1);
                        printer.disconnect();
                        WebcontentConverter.logger.info("bytes.length ${bytes.length}");
                      }
                    },
                  ),
                  ElevatedButton(
                    child: const Text('Print from URL'),
                    onPressed: () async {
                      // if (_formKey.currentState.validate()) {
                      //   _formKey.currentState.save();
                      //   var response = await HttpClient().getUrl(Uri.parse(_htmlText));
                      //   var document = parse(await response.transform(utf8.decoder).join());
                      //   await Printing.layoutPdf(
                      //     onLayout: (_) async => document.documentElement.outerHtml.codeUnits,
                      //   );
                      // }
                    },
                  ),
                  ElevatedButton(
                    child: const Text('HTML to PDF'),
                    onPressed: () async {},
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}