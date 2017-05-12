import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent implements OnInit {

  constructor() {
  }

  ngOnInit() {
  }

  onSelect({selected}) {
    console.log('Select Event', selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);
  }

}
