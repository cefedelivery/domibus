import {Routes, RouterModule} from "@angular/router";
import {LoginComponent} from "./login/login.component";
import {HomeComponent} from "./home/home.component";
import {AuthenticatedGuard} from "./guards/authenticated.guard";
import {AuthorizedGuard} from "./guards/authorized.guard";
import {ErrorLogComponent} from "./errorlog/errorlog.component";
import {PModeComponent} from "./pmode/pmode.component";

const appRoutes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthenticatedGuard] },
  { path: 'pmode', component: PModeComponent, canActivate: [AuthenticatedGuard, AuthorizedGuard], data:{allowedRoles: ["ROLE_ADMIN"]} },
  { path: 'errorlog', component: ErrorLogComponent, canActivate: [AuthenticatedGuard]},
  { path: 'login', component: LoginComponent }
];

export const routing = RouterModule.forRoot(appRoutes);
