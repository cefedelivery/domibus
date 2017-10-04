import {BackendFilterEntry} from "./backendfilterentry";
export class MessageFilterResult {

  constructor(public messageFilterEntries: Array<BackendFilterEntry>, public areFiltersPersisted: boolean) {
  }
}
