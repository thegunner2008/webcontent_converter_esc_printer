import 'dart:typed_data';

import 'package:esc_pos_printer/esc_pos_printer.dart';
import 'package:esc_pos_utils/esc_pos_utils.dart';
import 'package:flutter/material.dart';
import 'package:image/image.dart' as print_img;
import 'package:webcontent_converter_esc_printer/demo.dart';
import 'package:webcontent_converter_esc_printer/webcontent_converter_esc_printer.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  MainScreenState createState() => MainScreenState();
}

class MainScreenState extends State<MainScreen> {
  String ipAddress = '172.20.177.250';
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
                  SizedBox(
                    width: 100,
                    height: 400,
                    child: FutureBuilder(
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
                  ),
                  ElevatedButton(
                    child: const Text('Print from HTML'),
                    onPressed: () async {
                      var executablePath =
                      WebViewHelper.isChromeAvailable ? WebViewHelper.executablePath() : null;
                      var bytes = await WebcontentConverter.contentToImage(
                        content: _htmlText,
                        executablePath: executablePath,
                        // scale: 3,
                        width: 558,
                      );

                      final printer =
                          NetworkPrinter(PaperSize.mm80, await CapabilityProfile.load());
                      final connectPrinter = await printer.connect(ipAddress, port: 9100);

                      if (bytes.isNotEmpty) {
                        final img = print_img.decodeImage(bytes);
                        printer.image(img!);
                        printer.feed(1);
                        printer.disconnect();
                      }
                    },
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