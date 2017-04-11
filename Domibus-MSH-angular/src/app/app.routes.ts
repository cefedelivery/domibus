import {Routes, RouterModule} from "@angular/router";
import {LoginComponent} from "./login/login.component";
import {HomeComponent} from "./home/home.component";
import {AuthenticatedGuard} from "./guards/authenticated.guard";
import {ErrorLogComponent} from "./errorlog/errorlog.component";
import {PModeComponent} from "./pmode/pmode.component";
import {AuthorizedAdminGuard} from "./guards/authorized-admin.guard";
import {MessageLogComponent} from "./messagelog/messagelog.component";

const appRoutes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthenticatedGuard] },
  { path: 'pmode', component: PModeComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard] },
  { path: 'messagelog', component: MessageLogComponent, canActivate: [AuthenticatedGuard]},
  { path: 'errorlog', component: ErrorLogComponent, canActivate: [AuthenticatedGuard]},
  { path: 'login', component: LoginComponent },
  { path: '**', component: HomeComponent, canActivate: [AuthenticatedGuard] },
];

export const routing = RouterModule.forRoot(appRoutes);
