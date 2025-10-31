# Coffee Beans SCM 프로젝트
![image](https://github.com/user-attachments/assets/1d52fed3-58eb-4922-b2f1-67e592136047)

<br />
<br />

## 프로젝트 실행 방법
### 프로젝트 실행 전 유의 사항
Redis를 별도로 설치하거나 Docker를 통하여 아래 명령어 실행
```
docker run -d --name redis -p 6379:6379 -v redis-data:/data redis
```

### 클라이언트 실행
```
cd client
npm i
```
이후
```
npm run dev
```

### 서버 실행

Spring Boot Dashboard를 사용하여 실행
(server 폴더 내부 pom.xml 프로젝트 리로드)

root 폴더 내에 .vscode 폴더 내부에 launch.json
```
"vmArgs": "-Djasypt.encryptor.password=복호화키"
```
추가 후 다시 실행
<br />
<br />

## 프로젝트 개요
본사 주도로 각 프랜차이즈 가맹점 (판매처)의
커피 원두 발주 및 재고 / 출하 관리를 효율적으로 운영하기 위한 SCM

<br />
<br />

## 팀 구성
<table>
<tbody>
<tr>
<td align="center"><img src="https://github.com/user-attachments/assets/8e84f79c-1163-4ef1-a711-770272911d96" width="120" /></td>
<td align="center"><img src="https://github.com/user-attachments/assets/f5c53ecb-ba22-40ce-b2fa-68dd5fd4d0f1" width="120" /></td>
<td align="center"><img src="https://github.com/user-attachments/assets/d9f61d82-cf7b-4d07-a921-4228bead4f44" width="120" /></td>
<td align="center"><img src="https://github.com/user-attachments/assets/ffae07e9-91e5-4ae1-9043-afbce0a27bd1" width="120" /></td>
<td align="center"><img src="https://github.com/user-attachments/assets/dcc939a0-a017-4901-883b-c05a134bbdc0" width="120" /></td>
</tr>
<tr>
<th align="center">김대환</th>
<th align="center">김민서</th>
<th align="center">김동원</th>
<th align="center">김상우</th>
<th align="center">제갈은경</th>
</tr>
<tr>
<td align="center" width="120"><a href="https://github.com/mulumpyo">@mulumpyo</a></td>
<td align="center" width="120"><a href="https://github.com/min-seo7">@min-seo7</a></td>
<td align="center" width="120"><a href="https://github.com/tuna2510">@tuna2510</a></td>
<td align="center" width="120"><a href="https://github.com/ksw5861">@ksw5861</a></td>
<td align="center" width="120"><a href="https://github.com/a24826946">@a24826946</a></td>
</tr>
</tbody>
</table>

<br />
<br />

## 기술 스택
[![stackticon](https://firebasestorage.googleapis.com/v0/b/stackticon-81399.appspot.com/o/images%2F1761879514125?alt=media&token=c9ae9d32-6040-46ac-9ad1-3a34ed5efb4a)](https://github.com/msdio/stackticon)

<br />
<br />

## 개발 일정 타임라인

기간 | 작업 항목 | 담당 산출물 |
 :---: | :---: |  :---: |
2025.09.10 ~ 2025.09.11 | **요구사항 정의** | 요구사항 정의서 |
2025.09.12 ~ 2025.09.16 | **DB 설계** | ERD Cloud |
2025.09.17 ~ 2025.09.19 | **화면 설계** | 화면 설계서 |
2025.09.22 ~ 2025.10.20 | **구현 (개발)** | 소스 코드, 단위 테스트 명세서 |
2025.10.21 ~ 2025.10.23 | **통합 테스트** | 단위 테스트 명세서 (결과), 통합 테스트 보고서 |
2025.10.24 | **배포** |  배포 계획 및 결과 |

<br />
<br />

## 폴더 구조
### App
📦lib<br />
 ┣ 📂screens<br />
 ┃ ┣ 📜login.dart<br />
 ┃ ┣ 📜order_list.dart<br />
 ┃ ┗ 📜splash.dart<br />
 ┗ 📜main.dart<br />
<br />
### Client
📦src<br />
 ┣ 📂assets<br />
 ┃ ┣ 📂demo<br />
 ┃ ┃ ┣ 📂flags<br />
 ┃ ┃ ┃ ┗ 📜flags.css<br />
 ┃ ┃ ┣ 📜code.scss<br />
 ┃ ┃ ┗ 📜demo.scss<br />
 ┃ ┣ 📂layout<br />
 ┃ ┃ ┣ 📂variables<br />
 ┃ ┃ ┃ ┣ 📜_common.scss<br />
 ┃ ┃ ┃ ┣ 📜_dark.scss<br />
 ┃ ┃ ┃ ┗ 📜_light.scss<br />
 ┃ ┃ ┣ 📜layout.scss<br />
 ┃ ┃ ┣ 📜_core.scss<br />
 ┃ ┃ ┣ 📜_footer.scss<br />
 ┃ ┃ ┣ 📜_main.scss<br />
 ┃ ┃ ┣ 📜_menu.scss<br />
 ┃ ┃ ┣ 📜_mixins.scss<br />
 ┃ ┃ ┣ 📜_preloading.scss<br />
 ┃ ┃ ┣ 📜_responsive.scss<br />
 ┃ ┃ ┣ 📜_topbar.scss<br />
 ┃ ┃ ┣ 📜_typography.scss<br />
 ┃ ┃ ┗ 📜_utils.scss<br />
 ┃ ┣ 📜logo.png<br />
 ┃ ┣ 📜logo.webp<br />
 ┃ ┣ 📜styles.scss<br />
 ┃ ┗ 📜tailwind.css<br />
 ┣ 📂components<br />
 ┃ ┣ 📂card<br />
 ┃ ┃ ┣ 📂five<br />
 ┃ ┃ ┃ ┗ 📜ListCard.vue<br />
 ┃ ┃ ┗ 📜SearchCard.vue<br />
 ┃ ┣ 📂common<br />
 ┃ ┃ ┣ 📜AddressModal.vue<br />
 ┃ ┃ ┣ 📜Btn.vue<br />
 ┃ ┃ ┣ 📜checkBoxTable.vue<br />
 ┃ ┃ ┣ 📜ConfirmDialog.vue<br />
 ┃ ┃ ┣ 📜CountUp.vue<br />
 ┃ ┃ ┣ 📜DTable.vue<br />
 ┃ ┃ ┣ 📜Modal.vue<br />
 ┃ ┃ ┗ 📜SearchBox.vue<br />
 ┃ ┣ 📂dashboard<br />
 ┃ ┃ ┣ 📜BestSellingWidget.vue<br />
 ┃ ┃ ┣ 📜NotificationsWidget.vue<br />
 ┃ ┃ ┣ 📜RecentSalesWidget.vue<br />
 ┃ ┃ ┣ 📜RevenueStreamWidget.vue<br />
 ┃ ┃ ┗ 📜StatsWidget.vue<br />
 ┃ ┣ 📂landing<br />
 ┃ ┃ ┣ 📜FeaturesWidget.vue<br />
 ┃ ┃ ┣ 📜FooterWidget.vue<br />
 ┃ ┃ ┣ 📜HeroWidget.vue<br />
 ┃ ┃ ┣ 📜HighlightsWidget.vue<br />
 ┃ ┃ ┣ 📜PricingWidget.vue<br />
 ┃ ┃ ┗ 📜TopbarWidget.vue<br />
 ┃ ┗ 📜FloatingConfigurator.vue<br />
 ┣ 📂composables<br />
 ┃ ┣ 📜useAppToast.js<br />
 ┃ ┣ 📜useCountUp.js<br />
 ┃ ┣ 📜useFormat.js<br />
 ┃ ┗ 📜useIcon.js<br />
 ┣ 📂config<br />
 ┃ ┗ 📂menus<br />
 ┃ ┃ ┣ 📂menu<br />
 ┃ ┃ ┃ ┣ 📂dh<br />
 ┃ ┃ ┃ ┃ ┣ 📜account.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜dashboard.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜employees.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜notice.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜test.js<br />
 ┃ ┃ ┃ ┃ ┗ 📜vendor.js<br />
 ┃ ┃ ┃ ┣ 📂dw<br />
 ┃ ┃ ┃ ┃ ┣ 📜accountLedger.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜ledgerDashboard.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜orderApproval.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜orderInquiry.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜productInbound.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜returnHistory.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜returnProcess.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜shipPlan.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜shipRegister.js<br />
 ┃ ┃ ┃ ┃ ┗ 📜stockByProduct.js<br />
 ┃ ┃ ┃ ┣ 📂eg<br />
 ┃ ┃ ┃ ┃ ┣ 📜branchPos.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜insertOrder.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜insertPay.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜insertReturn.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜orderList.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜payList.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜posSetting.js<br />
 ┃ ┃ ┃ ┃ ┗ 📜returnList.js<br />
 ┃ ┃ ┃ ┣ 📂ms<br />
 ┃ ┃ ┃ ┃ ┣ 📜materialReceip.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜matLotStock.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜matOutboundRegistration.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜matStockadj.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜matUnload.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜planList.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜productionPlan.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜purchase.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜purchaseList.js<br />
 ┃ ┃ ┃ ┃ ┗ 📜purchaseOrder.js<br />
 ┃ ┃ ┃ ┣ 📂supply<br />
 ┃ ┃ ┃ ┃ ┣ 📜dashboard.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜matShipment.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜matSupply.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜requestList.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜requestResult.js<br />
 ┃ ┃ ┃ ┃ ┗ 📜supplyList.js<br />
 ┃ ┃ ┃ ┣ 📂sw<br />
 ┃ ┃ ┃ ┃ ┣ 📜bom.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜changeName.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜material.js<br />
 ┃ ┃ ┃ ┃ ┣ 📜product.js<br />
 ┃ ┃ ┃ ┃ ┗ 📜wareHouse.js<br />
 ┃ ┃ ┃ ┣ 📜customerOrder.js<br />
 ┃ ┃ ┃ ┣ 📜headOfficeOrder.js<br />
 ┃ ┃ ┃ ┣ 📜home.js<br />
 ┃ ┃ ┃ ┣ 📜logistics.js<br />
 ┃ ┃ ┃ ┣ 📜master.js<br />
 ┃ ┃ ┃ ┣ 📜order.js<br />
 ┃ ┃ ┃ ┣ 📜production.js<br />
 ┃ ┃ ┃ ┣ 📜stock.js<br />
 ┃ ┃ ┃ ┗ 📜supplier.js<br />
 ┃ ┃ ┗ 📜sideBar.js<br />
 ┣ 📂layout<br />
 ┃ ┣ 📂composables<br />
 ┃ ┃ ┗ 📜layout.js<br />
 ┃ ┣ 📜AppConfigurator.vue<br />
 ┃ ┣ 📜AppFooter.vue<br />
 ┃ ┣ 📜AppLayout.vue<br />
 ┃ ┣ 📜AppMenu.vue<br />
 ┃ ┣ 📜AppMenuItem.vue<br />
 ┃ ┣ 📜AppSidebar.vue<br />
 ┃ ┗ 📜AppTopbar.vue<br />
 ┣ 📂router<br />
 ┃ ┣ 📂routes<br />
 ┃ ┃ ┣ 📜dh.js<br />
 ┃ ┃ ┣ 📜dw.js<br />
 ┃ ┃ ┣ 📜eg.js<br />
 ┃ ┃ ┣ 📜login.js<br />
 ┃ ┃ ┣ 📜ms.js<br />
 ┃ ┃ ┣ 📜supplier.js<br />
 ┃ ┃ ┗ 📜sw.js<br />
 ┃ ┗ 📜index.js<br />
 ┣ 📂service<br />
 ┃ ┣ 📜CountryService.js<br />
 ┃ ┣ 📜CustomerService.js<br />
 ┃ ┣ 📜NodeService.js<br />
 ┃ ┣ 📜PhotoService.js<br />
 ┃ ┗ 📜ProductService.js<br />
 ┣ 📂stores<br />
 ┃ ┗ 📜user.js<br />
 ┣ 📂utils<br />
 ┃ ┗ 📜menuFilter.js<br />
 ┣ 📂views<br />
 ┃ ┣ 📂pages<br />
 ┃ ┃ ┣ 📂auth<br />
 ┃ ┃ ┃ ┣ 📜Access.vue<br />
 ┃ ┃ ┃ ┣ 📜Error.vue<br />
 ┃ ┃ ┃ ┗ 📜Login.vue<br />
 ┃ ┃ ┣ 📂dh<br />
 ┃ ┃ ┃ ┣ 📂components<br />
 ┃ ┃ ┃ ┃ ┗ 📂Notice<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📜Detail.vue<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📜Form.vue<br />
 ┃ ┃ ┃ ┃ ┃ ┗ 📜List.vue<br />
 ┃ ┃ ┃ ┣ 📜AccountManagement.vue<br />
 ┃ ┃ ┃ ┣ 📜ChangePassword.vue<br />
 ┃ ┃ ┃ ┣ 📜DevLogin.vue<br />
 ┃ ┃ ┃ ┣ 📜EmployeeManagement.vue<br />
 ┃ ┃ ┃ ┣ 📜Login.vue<br />
 ┃ ┃ ┃ ┣ 📜Notice.vue<br />
 ┃ ┃ ┃ ┣ 📜test.vue<br />
 ┃ ┃ ┃ ┗ 📜Vendor.vue<br />
 ┃ ┃ ┣ 📂dw<br />
 ┃ ┃ ┃ ┣ 📜AccountLedger.vue<br />
 ┃ ┃ ┃ ┣ 📜LedgerDashboard.vue<br />
 ┃ ┃ ┃ ┣ 📜OrderApproval.vue<br />
 ┃ ┃ ┃ ┣ 📜OrderInquiry.vue<br />
 ┃ ┃ ┃ ┣ 📜OrderShipment.vue<br />
 ┃ ┃ ┃ ┣ 📜ProductInbound.vue<br />
 ┃ ┃ ┃ ┣ 📜ReturnHistory.vue<br />
 ┃ ┃ ┃ ┣ 📜ReturnProcess.vue<br />
 ┃ ┃ ┃ ┣ 📜ShipPlan.vue<br />
 ┃ ┃ ┃ ┣ 📜ShipRegister.vue<br />
 ┃ ┃ ┃ ┗ 📜StockByProduct.vue<br />
 ┃ ┃ ┣ 📂eg<br />
 ┃ ┃ ┃ ┣ 📜BranchDash.vue<br />
 ┃ ┃ ┃ ┣ 📜BranchPos.vue<br />
 ┃ ┃ ┃ ┣ 📜GoDel.vue<br />
 ┃ ┃ ┃ ┣ 📜GoneDel.vue<br />
 ┃ ┃ ┃ ┣ 📜InsertOrder.vue<br />
 ┃ ┃ ┃ ┣ 📜InsertPay.vue<br />
 ┃ ┃ ┃ ┣ 📜InsertReturn.vue<br />
 ┃ ┃ ┃ ┣ 📜OrderList.vue<br />
 ┃ ┃ ┃ ┣ 📜PayList.vue<br />
 ┃ ┃ ┃ ┣ 📜PosSetting.vue<br />
 ┃ ┃ ┃ ┗ 📜ReturnList.vue<br />
 ┃ ┃ ┣ 📂ms<br />
 ┃ ┃ ┃ ┣ 📜MaterialReceip.vue<br />
 ┃ ┃ ┃ ┣ 📜MatLotStock.vue<br />
 ┃ ┃ ┃ ┣ 📜matOutboundRegistration.vue<br />
 ┃ ┃ ┃ ┣ 📜MatStockAdj.vue<br />
 ┃ ┃ ┃ ┣ 📜MatUnload.vue<br />
 ┃ ┃ ┃ ┣ 📜PlanList.vue<br />
 ┃ ┃ ┃ ┣ 📜ProductionPlan.vue<br />
 ┃ ┃ ┃ ┣ 📜Purchase.vue<br />
 ┃ ┃ ┃ ┣ 📜PurchaseList.vue<br />
 ┃ ┃ ┃ ┗ 📜PurchaseOrder.vue<br />
 ┃ ┃ ┣ 📂supplier<br />
 ┃ ┃ ┃ ┣ 📜MatShipment.vue<br />
 ┃ ┃ ┃ ┣ 📜matSupply.vue<br />
 ┃ ┃ ┃ ┣ 📜RequestList.vue<br />
 ┃ ┃ ┃ ┣ 📜RequestResult.vue<br />
 ┃ ┃ ┃ ┣ 📜SupplierDashboard.vue<br />
 ┃ ┃ ┃ ┗ 📜SupplyList.vue<br />
 ┃ ┃ ┣ 📂sw<br />
 ┃ ┃ ┃ ┣ 📜Bom.vue<br />
 ┃ ┃ ┃ ┣ 📜Material.vue<br />
 ┃ ┃ ┃ ┣ 📜Product.vue<br />
 ┃ ┃ ┃ ┗ 📜WareHouse.vue<br />
 ┃ ┃ ┣ 📜Crud.vue<br />
 ┃ ┃ ┣ 📜Documentation.vue<br />
 ┃ ┃ ┣ 📜Empty.vue<br />
 ┃ ┃ ┣ 📜Landing.vue<br />
 ┃ ┃ ┣ 📜NotFound.vue<br />
 ┃ ┃ ┣ 📜Test.vue<br />
 ┃ ┃ ┗ 📜Test2.html<br />
 ┃ ┗ 📜Dashboard.vue<br />
 ┣ 📜App.vue<br />
 ┗ 📜main.js<br />
<br />
### Server
📦src<br />
 ┣ 📂main<br />
 ┃ ┣ 📂java<br />
 ┃ ┃ ┗ 📂com<br />
 ┃ ┃ ┃ ┗ 📂yedam<br />
 ┃ ┃ ┃ ┃ ┗ 📂scm<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂common<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜KakaoAddressServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜MailServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜KakaoAddressService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜MailService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FileUploadProperties.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜KakaoProperties.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MailPropertiesConfig.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RedisConfig.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂dto<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountLedgerSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountListRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AddressRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AdjStockDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ApprovalSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ApproveMatSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmailDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmployeeListRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmployeeSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InboundListRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatInboundregisterDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatIssueLineResult.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatIssueRegisterRequest.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatIssueRegisterResponse.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatStockSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜matSupplySearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatUnloadSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NoticeListRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NoticeSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PageDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜prodPlanForAccoDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProdStockDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PurchaseListSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜purchaseOrderDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RecaptchaRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorChartDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorDashboardSummaryDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorDefectDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorRecentListDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorSearchDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜WarehouseListRes.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜WonjangReportDTO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂img<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ImgMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ImgServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ImgService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂instockMat<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜InStockMatMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜InStockMatServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜InStockMatService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂login<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜LoginMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AppLoginServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RecaptchaServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AppLoginService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RecaptchaService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂master<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BomMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmployeeMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialVendor.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProductMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UnitMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜WareHouseMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜WareHouseMapper1.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BomServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmployeeServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialVendorServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProductServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UnitServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜WareHouseServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜WareHouseServiceImpl1.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BomService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmployeeService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialVendorService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProductService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UnitService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜WareHouseService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜WareHouseService1.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂notice<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜NoticeMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜NoticeServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜NoticeService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂order<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BranchDashMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoDelMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜OrderMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PayMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReturnMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SalesMarginMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BranchDashServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoDelServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜OrderServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PayServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReturnServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SalesMarginServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BranchDashService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ExcelService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoDelService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜IamportService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜OrderService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PayService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReturnService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SalesMarginService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂outboundMat<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatIssueMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PlanMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ReqMatMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatIssueServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PlanServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ReqMatServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatIssueService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PlanService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ReqMatService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂product<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜InboundMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜InboundServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InboundService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜JasperReportService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂purchaseMat<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜PurchaseMatMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜PurchaseMatServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜PurchaseMatService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂security<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtBlacklistFilter.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtProperties.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtUtil.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityConfig.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂StockByProduct<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StockByProductMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StockByProductServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StockByProductService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂supplier<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SupplierMapper.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂impl<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SupplierServiceImpl.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SupplierService.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂vo<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AccountVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BomDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BomVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EmployeeVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InboundDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InboundLogVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InboundVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemInboundVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ItemOutboundVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialVendorVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MaterialVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatLotStockAdjVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatLotVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatStatusVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MatVendorVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MrpDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NoticeVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PaymentDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PaymentVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PrdPlanDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProductionPlanVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProductLotVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProductVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PurchaseMatVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PurStatusLogVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReqMatVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReturnDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReturnVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SalesDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SalesMarginVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SalesMasterVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SalesOrderDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SalesOrderVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ShipmentInfoVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ShipOrderDetailVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ShipOrderVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UnitVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜VendorVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜WareHouseVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ZApprovedShipmentRowVO.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📂web<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AddressController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜DhController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜DwController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜EgController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MsController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NoticeController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisTestController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SalesMarginController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SpaController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SupplierController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SwController.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📜JasyptConfig.java<br />
 ┃ ┃ ┃ ┃ ┃ ┣ 📜ScmApplication.java<br />
 ┃ ┃ ┃ ┃ ┃ ┗ 📜WebConfig.java<br />
 ┃ ┗ 📂resources<br />
 ┃ ┃ ┣ 📂mappers<br />
 ┃ ┃ ┃ ┣ 📜AccountMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜BomMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜BranchDashMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜EmployeeMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜GoDelMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜ImgMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜InStockMatMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜ItemInboundMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜LoginMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜MaterialMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜MaterialVendorMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜MatIssueMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜NoticeMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜OrderMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜PayMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜PlanMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜ProductMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜PurchaseMatMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜ReqMatMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜ReturnMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜SalesMarginMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜StockByProduct.xml<br />
 ┃ ┃ ┃ ┣ 📜SupplierMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜UnitMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜VendorMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜WareHouseMapper.xml<br />
 ┃ ┃ ┃ ┣ 📜WareHouseMapper1.xml<br />
 ┃ ┃ ┃ ┗ 📜ZMatCommonMapper.xml<br />
 ┃ ┃ ┣ 📂reports<br />
 ┃ ┃ ┃ ┣ 📜account_ledger_report.jrxml<br />
 ┃ ┃ ┃ ┣ 📜logo.png<br />
 ┃ ┃ ┃ ┣ 📜malgun-fonts.xml<br />
 ┃ ┃ ┃ ┣ 📜MALGUN.TTF<br />
 ┃ ┃ ┃ ┣ 📜MALGUNBD.TTF<br />
 ┃ ┃ ┃ ┣ 📜MALGUNSL.TTF<br />
 ┃ ┃ ┃ ┣ 📜order_detail.jasper<br />
 ┃ ┃ ┃ ┗ 📜ship.jasper<br />
 ┃ ┃ ┣ 📜application-prod.yml<br />
 ┃ ┃ ┣ 📜application.yml<br />
 ┃ ┃ ┗ 📜jasperreports_extension.properties<br />
 ┗ 📂test<br />
 ┃ ┗ 📂java<br />
 ┃ ┃ ┗ 📂com<br />
 ┃ ┃ ┃ ┗ 📂yedam<br />
 ┃ ┃ ┃ ┃ ┗ 📂scm<br />
 ┃ ┃ ┃ ┃ ┃ ┗ 📜ScmApplicationTests.java
<br />
<br />
