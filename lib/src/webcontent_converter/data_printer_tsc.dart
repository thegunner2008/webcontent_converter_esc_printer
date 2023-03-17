class DataPrinterTSC {
  static List<int> cls() {
    String str = "CLS\n";
    List<int> data = str.codeUnits.toList();
    return data;
  }

  static List<int> sizeByMm(double m, double n) {
    String str = "SIZE $m mm,$n mm\n";
    List<int> data = str.codeUnits.toList();
    return data;
  }

  static List<int> direction(int n) {
    String str = "DIRECTION $n\n";
    List<int> data = str.codeUnits.toList();
    return data;
  }

  static List<int> reference(int x, int y) {
    String str = "REFERENCE $x, $y\n";
    List<int> data = str.codeUnits.toList();
    return data;
  }

  static List<int> print(int m, int n) {
    String str = "PRINT $m,$n\n";
    List<int> data = str.codeUnits.toList();
    return data;
  }
}