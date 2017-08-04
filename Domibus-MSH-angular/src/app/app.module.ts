import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Http, HttpModule, RequestOptions, XHRBackend} from "@angular/http";
import {
  MdButtonModule,
  MdDialogModule,
  MdIconModule,
  MdInputModule,
  MdListModule,
  MdMenuModule,
  MdSelectModule,
  MdSidenavModule,
  MdTooltipModule
} from "@angular/material";
import "hammerjs";

import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {Md2Module, Md2SelectModule} from "md2";

import {AppComponent} from "./app.component";
import {LoginComponent} from "./login/login.component";
import {HomeComponent} from "./home/home.component";
import {PModeComponent} from "./pmode/pmode.component";

import {AuthenticatedGuard} from "./guards/authenticated.guard";
import {AuthorizedGuard} from "./guards/authorized.guard";
import {routing} from "./app.routes";
import {IsAuthorized} from "./security/is-authorized.directive";
import {ExtendedHttpClient} from "./http/extended-http-client";
import {HttpEventService} from "./http/http.event.service";
import {SecurityService} from "./security/security.service";
import {SecurityEventService} from "./security/security.event.service";
import {AlertComponent} from "./alert/alert.component";
import {AlertService} from "./alert/alert.service";
import {ErrorLogComponent} from "./errorlog/errorlog.component";
import {FooterComponent} from "./footer/footer.component";
import {DomibusInfoService} from "./appinfo/domibusinfo.service";
import {AuthorizedAdminGuard} from "./guards/authorized-admin.guard";
import {MessageFilterComponent} from "./messagefilter/messagefilter.component";
import {MessageLogComponent} from "./messagelog/messagelog.component";
import {UserComponent} from "./user/user.component";
import {TruststoreComponent} from "./truststore/truststore.component";
import {PmodeUploadComponent} from "./pmode-upload/pmode-upload.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MessagelogDialogComponent} from "./messagelog/messagelog-dialog/messagelog-dialog.component";
import {JmsComponent} from "./jms/jms.component";
import {RowLimiterComponent} from "./common/row-limiter/row-limiter.component";
import {MoveDialogComponent} from "./jms/move-dialog/move-dialog.component";
import {MessageDialogComponent} from "./jms/message-dialog/message-dialog.component";
import {DatePipe} from "./customDate/datePipe";
import {DefaultPasswordDialogComponent} from "./security/default-password-dialog/default-password-dialog.component";
import {MessagelogDetailsComponent} from "./messagelog/messagelog-details/messagelog-details.component";
import {ErrorlogDetailsComponent} from "./errorlog/errorlog-details/errorlog-details.component";
import {EditMessageFilterComponent} from "./messagefilter/editmessagefilter-form/editmessagefilter-form.component";
import {CancelDialogComponent} from "./common/cancel-dialog/cancel-dialog.component";
import {DirtyGuard} from "./common/dirty.guard";
import {EditUserComponent} from "app/user/edituser-form/edituser-form.component";
import {SaveDialogComponent} from "./common/save-dialog/save-dialog.component";
import {TruststoreDialogComponent} from "./truststore/truststore-dialog/truststore-dialog.component";
import {TrustStoreUploadComponent} from "./truststore/truststore-upload/truststore-upload.component";
import {PageHelperComponent} from "./common/page-helper/page-helper.component";
import {HelpDialogComponent} from "./common/page-helper/help-dialog/help-dialog.component";
import {PmodeHelpComponent} from "./pmode/pmode-help/pmode-help.component";
import {JmsHelpComponent} from "./jms/jms-help/jms-help.component";
import {TruststoreHelpComponent} from "./truststore/truststore-help/truststore-help.component";
import {ErrorlogHelpComponent} from "./errorlog/errorlog-help/errorlog-help.component";
import {MessagelogHelpComponent} from "./messagelog/messagelog-help/messagelog-help.component";
import {MessagefilterHelpComponent} from "./messagefilter/messagefilter-help/messagefilter-help.component";
import {UserHelpComponent} from "./user/user-help/user-help.component";

export function extendedHttpClientFactory(xhrBackend: XHRBackend, requestOptions: RequestOptions, httpEventService: HttpEventService) {
  return new ExtendedHttpClient(xhrBackend, requestOptions, httpEventService);
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    MessageFilterComponent,
    MessageLogComponent,
    UserComponent,
    ErrorLogComponent,
    AlertComponent,
    FooterComponent,
    PModeComponent,
    IsAuthorized,
    TruststoreComponent,
    PmodeUploadComponent,
    SaveDialogComponent,
    MessagelogDialogComponent,
    CancelDialogComponent,
    JmsComponent,
    RowLimiterComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    DatePipe,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    MessagelogDetailsComponent,
    ErrorlogDetailsComponent,
    EditUserComponent,
    TruststoreDialogComponent,
    TrustStoreUploadComponent,
    PageHelperComponent,
    HelpDialogComponent,
    PmodeHelpComponent,
    JmsHelpComponent,
    TruststoreHelpComponent,
    ErrorlogHelpComponent,
    MessagelogHelpComponent,
    MessagefilterHelpComponent,
    UserHelpComponent
  ],
  entryComponents: [
    AppComponent,
    PmodeUploadComponent,
    MessagelogDialogComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    MessagelogDetailsComponent,
    CancelDialogComponent,
    SaveDialogComponent,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    ErrorlogDetailsComponent,
    EditUserComponent,
    TruststoreDialogComponent,
    TrustStoreUploadComponent,
    HelpDialogComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpModule,
    NgxDatatableModule,
    MdButtonModule,
    MdDialogModule,
    MdTooltipModule,
    MdMenuModule,
    MdInputModule,
    MdIconModule,
    MdListModule,
    MdSidenavModule,
    MdSelectModule,
    routing,
    ReactiveFormsModule,
    Md2Module,
    Md2SelectModule
  ],
  providers: [
    AuthenticatedGuard,
    AuthorizedGuard,
    AuthorizedAdminGuard,
    DirtyGuard,
    HttpEventService,
    SecurityService,
    SecurityEventService,
    DomibusInfoService,
    AlertService,
    {
      provide: Http,
      useFactory: extendedHttpClientFactory,
      deps: [XHRBackend, RequestOptions, HttpEventService],
      multi: false
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
