import {Component, Inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';
import {BackendFilterEntry} from '../backendfilterentry';
import {isNullOrUndefined} from 'util';

let NEW_MODE = 'New Message Filter';
let EDIT_MODE = 'Message Filter Edit';
let MAX_LENGTH = 255;

@Component({
  selector: 'editmessagefilter-form',
  templateUrl: 'editmessagefilter-form.component.html'
})
export class EditMessageFilterComponent {

  plugin: string;
  from: string;
  to: string;
  service: string;
  action: string;
  formTitle: string = EDIT_MODE;
  textMaxLength = MAX_LENGTH;

  backendFilterNames: Array<String> = [];

  messageFilterForm: FormGroup;

  constructor(public dialogRef: MdDialogRef<EditMessageFilterComponent>,
              @Inject(MD_DIALOG_DATA) public data: any,
              fb: FormBuilder) {
    if (isNullOrUndefined(data.edit)) {
      this.formTitle = NEW_MODE;
      this.backendFilterNames = data.backendFilterNames;
      this.plugin = this.backendFilterNames[0].toString();
      this.from = '';
      this.to = '';
      this.action = '';
      this.service = '';
    } else {
      let backEntry: BackendFilterEntry = new BackendFilterEntry(this.data.edit.entityId,
        this.data.edit.index,
        this.data.edit.backendName,
        this.data.edit.routingCriterias,
        this.data.edit.persisted);
      this.backendFilterNames = data.backendFilterNames;
      this.plugin = backEntry.backendName;
      this.from = isNullOrUndefined(backEntry.from) ? '' : backEntry.from.expression;
      this.to = isNullOrUndefined(backEntry.to) ? '' : backEntry.to.expression;
      this.action = isNullOrUndefined(backEntry.action) ? '' : backEntry.action.expression;
      this.service = isNullOrUndefined(backEntry.service) ? '' : backEntry.service.expression;
    }
    this.messageFilterForm = fb.group({
      'plugin': [null, Validators.required],
      'from': [null, Validators.pattern],
      'to': [null, Validators.pattern],
      'action': [null, Validators.pattern],
      'service': [null, Validators.pattern]
    });
  }

  updatePlugin(event) {
    this.plugin = event.value;
  }

  updateFrom(event) {
    this.from = event.target.value;
  }

  updateTo(event) {
    this.to = event.target.value;
  }

  updateAction(event) {
    this.action = event.target.value;
  }

  updateService(event) {
    this.service = event.target.value;
  }

  submitForm() {
    this.dialogRef.close(true);
  }
}
