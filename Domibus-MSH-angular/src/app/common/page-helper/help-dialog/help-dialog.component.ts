import {
  Compiler,
  Component,
  ComponentFactory,
  ComponentRef,
  Inject,
  ModuleWithComponentFactories,
  NgModule,
  OnInit,
  ViewChild,
  ViewContainerRef
} from "@angular/core";
import {MD_DIALOG_DATA, MdDialogRef} from "@angular/material";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-help-dialog',
  templateUrl: './help-dialog.component.html',
  styleUrls: ['./help-dialog.component.css']
  /**
   * template: `
   <div>
   <div #container></div>
   </div>
   `,
   */
})
export class HelpDialogComponent implements OnInit {

  template: string = '<div>\nHello, {{name}}\n</div>';
  pageName: String;
  @ViewChild('container', {read: ViewContainerRef})
  container: ViewContainerRef;
  private componentRef: ComponentRef<{}>;


  constructor(public dialogRef: MdDialogRef<HelpDialogComponent>,
              @Inject(MD_DIALOG_DATA) public data: any,
              private compiler: Compiler) {
    this.pageName = data.pageName;
  }

  private createComponentFactorySync(compiler: Compiler, metadata: Component, componentClass: any): ComponentFactory<any> {
    const cmpClass = componentClass || class HelpDialogComponent {
        name: string = 'Denys'
      };
    const decoratedCmp = Component(metadata)(cmpClass);

    @NgModule({imports: [CommonModule], declarations: [decoratedCmp]})
    class RuntimeComponentModule {
    }

    let module: ModuleWithComponentFactories<any> = compiler.compileModuleAndAllComponentsSync(RuntimeComponentModule);
    return module.componentFactories.find(f => f.componentType === decoratedCmp);
  }

  compileTemplate() {
    let metadata = {
      selector: `runtime-help`,
      templateUrl: '../../../' + this.pageName + '/help/help.html'

    }
    let factory = this.createComponentFactorySync(this.compiler, metadata, null);
    if (this.componentRef) {
      this.componentRef.destroy();
      this.componentRef = null;
    }
    this.componentRef = this.container.createComponent(factory);
  }

  ngOnInit() {
    this.compileTemplate();
  }

}
