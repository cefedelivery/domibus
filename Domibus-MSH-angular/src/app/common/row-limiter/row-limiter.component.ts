import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-row-limiter',
  templateUrl: './row-limiter.component.html',
  styleUrls: ['./row-limiter.component.css']
})
export class RowLimiterComponent implements OnInit {

  @Input()
  pageSizes: Array<any>;

  @Output()
  private onPageSizeChanged = new EventEmitter<number>();

  pageSize: number;

  constructor() {
  }

  ngOnInit() {
    this.pageSize = this.pageSizes[0].value;
  }

  changePageSize(newPageLimit:number) {
    this.onPageSizeChanged.emit(newPageLimit);
    console.log('New page limit:', newPageLimit);
  }

}
