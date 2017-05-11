import {Routes, RouterModule} from "@angular/router";
import {LoginComponent} from "./login/login.component";
import {AuthenticatedGuard} from "./guards/authenticated.guard";
import {ErrorLogComponent} from "./errorlog/errorlog.component";
import {PModeComponent} from "./pmode/pmode.component";
import {AuthorizedAdminGuard} from "./guards/authorized-admin.guard";
import {MessageFilterComponent} from "./messagefilter/messagefilter.component";
import {MessageLogComponent} from "./messagelog/messagelog.component";
import {TruststoreComponent} from "app/truststore/truststore.component";

const appRoutes: Routes = [
  {path: '', component: MessageLogComponent, canActivate: [AuthenticatedGuard]},
  {path: 'pmode', component: PModeComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard]},
  {path: 'messagefilter', component: MessageFilterComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard]},
  {path: 'truststore', component: TruststoreComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard]},
  {path: 'messagelog', component: MessageLogComponent, canActivate: [AuthenticatedGuard]},
  {path: 'errorlog', component: ErrorLogComponent, canActivate: [AuthenticatedGuard]},
  {path: 'login', component: LoginComponent},
  {path: '**', component: MessageLogComponent, canActivate: [AuthenticatedGuard]},
];

export const routing = RouterModule.forRoot(appRoutes);
