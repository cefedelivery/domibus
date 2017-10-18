export class AuditResponseRo {
  id: string;
  revisionId: string;
  auditTargetName: string;
  action: string;
  user: string;
  changed: string;

  constructor(id: string, revisionId: string, auditTargetName: string, action: string, user: string, changed: string) {
    this.id = id;
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
