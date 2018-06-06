/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Rest entry point to retrieve the audit logs.
 */
export class PartyResponseRo {

  entityId: number;

  identifiers: Array<IdentifierRo>; //NOSONAR

  name: string;

  endpoint: string;

  processes;

  joinedIdentifiers: string;

  joinedProcesses: string;

  processesWithPartyAsInitiator: Array<ProcessRo>;

  processesWithPartyAsResponder: Array<ProcessRo>;

  certificate: CertificateRo;
}

export class IdentifierRo {

  entityId: number;

  partyId: string;

  partyIdType: PartyIdTypeRo;

}

export class PartyIdTypeRo {

  name: string;

  value: string;

}

export class ProcessRo {

  entityId: number;

  name: string;

}

export class ProcessInfoRo {

  name: string;

  isInitiator: boolean;

  isResponder: boolean;
}

export class PartyFilteredResult {
  data: PartyResponseRo[];

  allData: PartyResponseRo[];
}

export class CertificateRo {

  subjectName: string;

  validityFrom: Date;

  validityTo: Date;

  issuer: string;

  fingerprints: string;
}
