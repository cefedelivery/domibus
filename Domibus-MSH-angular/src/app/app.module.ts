import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule, Http, XHRBackend, RequestOptions} from '@angular/http';
import {
  MdDialogModule, MdIconModule, MdInputModule, MdMenuModule, MdSelectModule,
  MdSidenavModule, MdButtonModule, MdListModule
} from '@angular/material';
import 'hammerjs';

import {NgxDatatableModule} from '@swimlane/ngx-datatable';
import {Md2Module, Md2SelectModule} from 'md2';

import {AppComponent} from './app.component';
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
import {UserComponent} from "./user/user.component"
import {TruststoreComponent} from './truststore/truststore.component';
import {PmodeUploadComponent} from './pmode-upload/pmode-upload.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MessagelogDialogComponent} from './messagelog/messagelog-dialog/messagelog-dialog.component';
import {JmsComponent} from './jms/jms.component';
import {RowLimiterComponent} from './common/row-limiter/row-limiter.component';
import {MoveDialogComponent} from "./jms/move-dialog/move-dialog.component";
import {MessageDialogComponent} from './jms/message-dialog/message-dialog.component';
import {PasswordComponent} from './user/password/password-dialog.component';
import {DatePipe} from './customDate/datePipe';
import {DefaultPasswordDialogComponent} from './security/default-password-dialog/default-password-dialog.component';
import {MessagelogDetailsComponent} from './messagelog/messagelog-details/messagelog-details.component';
import {ErrorlogDetailsComponent} from './errorlog/errorlog-details/errorlog-details.component';
import {EditMessageFilterComponent} from "./messagefilter/editmessagefilter-form/editmessagefilter-form.component";
import {CancelDialogComponent} from './common/cancel-dialog/cancel-dialog.component';
import {DirtyGuard} from "./common/dirty.guard";
import {EditUserComponent} from "app/user/edituser-form/edituser-form.component";
import {MessagefilterDialogComponent} from "./dialogs/savedialog/savedialog.component";

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
    PModeComponent,
    PmodeUploadComponent,
    MessagefilterDialogComponent,
    PmodeUploadComponent,
    MessagelogDialogComponent,
    CancelDialogComponent,
    JmsComponent,
    RowLimiterComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    PasswordComponent,
    DatePipe,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    MessagelogDetailsComponent,
    ErrorlogDetailsComponent,
    EditMessageFilterComponent,
    EditUserComponent
  ],
  entryComponents: [
    AppComponent,
    PmodeUploadComponent,
    MessagefilterDialogComponent,
    PmodeUploadComponent,
    MessagelogDialogComponent,
    MoveDialogComponent,
    MessageDialogComponent,
    MessagelogDetailsComponent,
    MessagelogDetailsComponent,
    CancelDialogComponent,
    MoveDialogComponent,
    PasswordComponent,
    DefaultPasswordDialogComponent,
    EditMessageFilterComponent,
    ErrorlogDetailsComponent,
    EditUserComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpModule,
    NgxDatatableModule,
    MdButtonModule,
    MdDialogModule,
    MdMenuModule,
    MdInputModule,
    MdIconModule,
    MdListModule,
    MdSidenavModule,
    MdSelectModule,
    routing,
    ReactiveFormsModule,
    Md2Module,
    ReactiveFormsModule,
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
