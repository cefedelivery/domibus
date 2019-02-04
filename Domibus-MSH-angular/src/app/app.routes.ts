import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './security/login/login.component';
import {AuthenticatedGuard} from './common/guards/authenticated.guard';
import {ErrorLogComponent} from './errorlog/errorlog.component';
import {CurrentPModeComponent} from './pmode/current/currentPMode.component';
import {PModeArchiveComponent} from './pmode/archive/pmodeArchive.component';
import {AuthorizedAdminGuard} from './common/guards/authorized-admin.guard';
import {MessageFilterComponent} from './messagefilter/messagefilter.component';
import {MessageLogComponent} from './messagelog/messagelog.component';
import {UserComponent} from './user/user.component';
import {TruststoreComponent} from 'app/truststore/truststore.component';
import {JmsComponent} from './jms/jms.component';
import {DirtyGuard} from './common/guards/dirty.guard';
import {AuditComponent} from './audit/audit.component';
import {PartyComponent} from './party/party.component';
import {AlertsComponent} from './alerts/alerts.component';
import {TestServiceComponent} from './testservice/testservice.component';
import {PluginUserComponent} from './pluginuser/pluginuser.component';
import {DefaultPasswordGuard} from './security/defaultPassword.guard';
import {AuthExternalProviderGuard} from './common/guards/auth-external-provider.guard';
import {LoggingComponent} from './logging/logging.component';
import {ChangePasswordComponent} from './security/change-password/change-password.component';
import {LogoutAuthExtProviderComponent} from "./security/logout/logout.components";
import {RedirectHomeGuard} from "./common/guards/redirect-home.guard";

const appRoutes: Routes = [
  {
    path: '',
    component: MessageLogComponent,
    canActivate: [AuthenticatedGuard, DefaultPasswordGuard]
  },
  {
    path: 'pmode-current',
    component: CurrentPModeComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard]
  },
  {
    path: 'pmode-archive',
    component: PModeArchiveComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard]
  },
  {
    path: 'pmode-party',
    component: PartyComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard]
  },
  {
    path: 'jms',
    component: JmsComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard]
  },
  {
    path: 'messagefilter',
    component: MessageFilterComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard]
  },
  {
    path: 'truststore',
    component: TruststoreComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard]
  },
  {
    path: 'messagelog',
    component: MessageLogComponent,
    canActivate: [AuthenticatedGuard, DefaultPasswordGuard]
  },
  {
    path: 'user',
    component: UserComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard, AuthExternalProviderGuard],
    canDeactivate: [DirtyGuard]
  },
  {
    path: 'pluginuser',
    component: PluginUserComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard]
  },
  {path: 'errorlog', component: ErrorLogComponent, canActivate: [AuthenticatedGuard, DefaultPasswordGuard]},
  {path: 'login', component: LoginComponent, canActivate: [AuthExternalProviderGuard, RedirectHomeGuard]},
  {path: 'audit', component: AuditComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard]},
  {path: 'alerts', component: AlertsComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard]},
  {path: 'testservice', component: TestServiceComponent, canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard]},
  {path: 'changePassword', component: ChangePasswordComponent, canActivate: [AuthenticatedGuard, AuthExternalProviderGuard]},
  {
    path: 'logging',
    component: LoggingComponent,
    canActivate: [AuthenticatedGuard, AuthorizedAdminGuard, DefaultPasswordGuard],
    data: {
      isDomainIndependent: true
    }
  },
  {path: 'logout', component: LogoutAuthExtProviderComponent},
  {path: '**', component: MessageLogComponent, canActivate: [AuthenticatedGuard, DefaultPasswordGuard]},

];

export const routing = RouterModule.forRoot(appRoutes);
