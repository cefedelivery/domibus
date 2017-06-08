//import {MessageFilterEntry} from "./messagefilterentry";
import {BackendFilterEntry} from "./backendfilterentry";
export class MessageFilterResult {
  public length: number;

  /*constructor(public messageFilterEntries: Array<MessageFilterEntry>) {

  }*/

  constructor(public backendFilterEntries: Array<BackendFilterEntry>) {
    this.length = backendFilterEntries.length;
  }
}
