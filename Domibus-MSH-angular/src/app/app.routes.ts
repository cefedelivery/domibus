import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './security/login/login.component';
import {AuthenticatedAuthorizedGuard} from './common/guards/authenticated-authorized.guard';
import {ErrorLogComponent} from './errorlog/errorlog.component';
import {CurrentPModeComponent} from './pmode/current/currentPMode.component';
import {PModeArchiveComponent} from './pmode/archive/pmodeArchive.component';
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
import {NotAuthorizedComponent} from "./security/not-authorized/not-authorized.components";
import {SecurityService} from "./security/security.service";


const appRoutes: Routes = [
  {
    path: '',
    component: MessageLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES
    }
  },
  {
    path: 'pmode-current',
    component: CurrentPModeComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'pmode-archive',
    component: PModeArchiveComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'pmode-party',
    component: PartyComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES
    }
  },
  {
    path: 'jms',
    component: JmsComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'messagefilter',
    component: MessageFilterComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'truststore',
    component: TruststoreComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'messagelog',
    component: MessageLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES
    }
  },
  {
    path: 'user',
    component: UserComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard, AuthExternalProviderGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'pluginuser',
    component: PluginUserComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    canDeactivate: [DirtyGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'errorlog',
    component: ErrorLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.USER_ROLES
    }
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [AuthExternalProviderGuard, RedirectHomeGuard]
  },
  {
    path: 'audit',
    component: AuditComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'alerts',
    component: AlertsComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'testservice',
    component: TestServiceComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'changePassword',
    component: ChangePasswordComponent,
    canActivate: [AuthenticatedAuthorizedGuard, AuthExternalProviderGuard]
  },
  {
    path: 'logging',
    component: LoggingComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard],
    data: {
      isDomainIndependent: true,
      checkRoles: SecurityService.ADMIN_ROLES
    }
  },
  {
    path: 'logout',
    component: LogoutAuthExtProviderComponent
  },
  {
    path: 'notAuthorized',
    component: NotAuthorizedComponent,
    canActivate: [AuthenticatedAuthorizedGuard]
  },
  {
    path: '**',
    component: MessageLogComponent,
    canActivate: [AuthenticatedAuthorizedGuard, DefaultPasswordGuard]
  },

];

export const routing = RouterModule.forRoot(appRoutes);
