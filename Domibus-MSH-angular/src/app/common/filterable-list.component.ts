import {Component, OnInit} from '@angular/core';

/**
 * Base class for components that display a list of items that can be filtered
 * It is an embrion; more common functionality will be added in time
 *
 * @since 4.1
 */

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

  /**
   * The method takes the filter params set through widgets and copies them to the active params
   * active params are the ones that are used for actual filtering of data and can be different from the ones set by the user in the UI
   */
  protected setActiveFilter() {
    if (!this.activeFilter) {
      this.activeFilter = {};
    }
    Object.assign(this.activeFilter, this.filter);
  }

  /**
   * The method takes the actual filter params and copies them to the UI bound params thus synchronizing the pair so what you see it is what you get
   */
  protected resetFilters() {
    this.filter = {};
    Object.assign(this.filter, this.activeFilter);
  }


}
