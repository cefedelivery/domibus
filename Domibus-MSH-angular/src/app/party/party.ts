/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Rest entry point to retrieve the audit logs.
 */
export class PartyResponseRo {

  entityId:string;

  identifiers; //NOSONAR

  name:string;

  endpoint:string;

  partyIDs:string;

  processes: ProcessRo[];

  joinedIdentifiers:string;

  joinedProcesses:string;

}

export class ProcessRo {

  entityId: number;
  name: string;

}


