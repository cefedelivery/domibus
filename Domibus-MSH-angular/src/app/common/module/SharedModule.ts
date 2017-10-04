import {NgModule} from "@angular/core";

import {ClickStopPropagationDirective} from "app/common/directive/attribute/ClickStopPropagation";

@NgModule({
  declarations: [
    ClickStopPropagationDirective
  ],
  exports: [
    ClickStopPropagationDirective
  ]
})
export class SharedModule {
}
