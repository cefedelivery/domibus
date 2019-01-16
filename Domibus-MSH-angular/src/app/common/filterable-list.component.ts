import {Component, OnInit} from '@angular/core';

@Component({
  moduleId: module.id,
  selector: 'filtered-list',
  template: '',
})

export class FilterableListComponent implements OnInit {
  public filter: any;
  public activeFilter: any;

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
