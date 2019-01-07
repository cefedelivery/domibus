import {NgModule} from "@angular/core";

import {ClickStopPropagationDirective} from 'app/common/directive/attribute/ClickStopPropagation';
import {ButtonClickBehaviourDirective} from '../directive/ButtonClickBehaviour';

@NgModule({
  declarations: [
    ClickStopPropagationDirective,
    ButtonClickBehaviourDirective
  ],
  exports: [
    ClickStopPropagationDirective,
    ButtonClickBehaviourDirective
  ]
})
export class SharedModule {
}
