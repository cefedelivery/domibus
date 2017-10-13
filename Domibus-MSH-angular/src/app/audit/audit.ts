export class AuditResponseRo {
  revisionId: string;
  auditTargetName: string;
  action: string;
  user: string;
  changed: string;

  constructor(revisionId: string, auditTargetName: string, action: string, user: string, changed: string) {
    this.revisionId = revisionId;
    this.auditTargetName = auditTargetName;
    this.action = action;
    this.user = user;
    this.changed = changed;
  }
}

export class AuditCriteria {
  auditTargetName: string[];
  action: string[];
  user: string[];
  from;
  to;
  start;
  max;
}
