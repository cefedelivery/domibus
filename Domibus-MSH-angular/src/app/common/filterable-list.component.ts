import {Component, OnInit} from '@angular/core';

@Component({
  moduleId: module.id,
  selector: 'filtered-list',
  template: '',
})

export class FilterableListComponent implements OnInit {
  protected filter: any;
  protected activeFilter: any;

  constructor() {
  }

  ngOnInit() {
    this.filter = {};
  }

  protected setActiveFilter() {
    if (!this.activeFilter) {
      this.activeFilter = {};
    }
    Object.assign(this.activeFilter, this.filter);
  }

  protected resetFilters() {
    this.filter = {};
    Object.assign(this.filter, this.activeFilter);
  }


}
