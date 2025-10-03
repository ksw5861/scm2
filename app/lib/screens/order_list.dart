import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:barcode_scan2/barcode_scan2.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

class OrderListScreen extends StatefulWidget {
  final String token;

  const OrderListScreen({super.key, required this.token});

  @override
  State<OrderListScreen> createState() => _OrderListScreenState();
}

class _OrderListScreenState extends State<OrderListScreen> {
  List<dynamic> orders = [];
  bool _isLoading = true;

  String get baseUrl => dotenv.env['BASE_URL']!;

  @override
  void initState() {
    super.initState();
    _fetchOrders();
  }

  /// 주문 목록 불러오기 (출고완료 상태만)
  Future<void> _fetchOrders() async {
    try {
      final response = await http.get(
        Uri.parse("$baseUrl/app/orderlist"),
        headers: {
          "Authorization": "Bearer ${widget.token}",
        },
      );

      if (response.statusCode == 200) {
        setState(() {
          orders = jsonDecode(response.body);
          _isLoading = false;
        });
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("주문 목록 불러오기 실패 (${response.statusCode})")),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("에러 발생: $e")),
      );
    }
  }

  /// 버튼 클릭 → 배송완료 처리 API 호출
  Future<bool> _completeOrder(String shipId) async {
    try {
      final response = await http.put(
        Uri.parse("$baseUrl/app/order/completed/$shipId"),
        headers: {
          "Authorization": "Bearer ${widget.token}",
        },
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  /// 바코드 스캔 → 출고완료 처리 API 호출
  Future<bool> _shipOutOrder(String shipId) async {
    try {
      final response = await http.put(
        Uri.parse("$baseUrl/app/order/shipout/$shipId"),
        headers: {
          "Authorization": "Bearer ${widget.token}",
        },
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  /// 바코드 스캔 → 성공하면 목록 새로고침, 실패 시 다시 스캔
  Future<void> _scanAndComplete() async {
    try {
      var result = await BarcodeScanner.scan();

      if (result.type == ResultType.Barcode) {
        final scannedValue = result.rawContent;
        if (scannedValue.isNotEmpty) {
          final success = await _shipOutOrder(scannedValue);

          if (success) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text("출고완료 처리 성공!")),
            );
            _fetchOrders(); // 목록 새로고침
          } else {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text("출고 처리 실패: 다시 스캔해주세요.")),
            );
            _scanAndComplete(); // 실패하면 재시도
          }
        }
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("바코드 스캔 실패: $e")),
      );
    }
  }

  /// 버튼 눌렀을 때 confirm → 배송완료 API 호출
  void _confirmAndComplete(String shipId) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text("배송완료 확인"),
        content: Text("주문 $shipId 건을 배송완료 처리하시겠습니까?"),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text("취소"),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              final success = await _completeOrder(shipId);
              if (success) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("배송완료 처리 성공!")),
                );
                _fetchOrders();
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("배송완료 처리 실패")),
                );
              }
            },
            child: const Text("확인"),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("배송중인 주문 목록"),
        backgroundColor: Colors.blue,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : orders.isEmpty
              ? const Center(child: Text("출고완료 상태의 주문이 없습니다."))
              : RefreshIndicator(
                  onRefresh: _fetchOrders,
                  child: ListView.builder(
                    physics: const AlwaysScrollableScrollPhysics(),
                    itemCount: orders.length,
                    itemBuilder: (context, index) {
                      final order = orders[index];
                      final shipId = order["shipId"].toString();

                      return Card(
                        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                        child: ListTile(
                          title: Text("출하번호: $shipId"),
                          trailing: ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.green,
                            ),
                            onPressed: () => _confirmAndComplete(shipId),
                            child: const Text("배송완료"),
                          ),
                        ),
                      );
                    },
                  ),
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _scanAndComplete,
        backgroundColor: Colors.blue,
        child: const Icon(Icons.qr_code_scanner),
      ),
    );
  }


}
