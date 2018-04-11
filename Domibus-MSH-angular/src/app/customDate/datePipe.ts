import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'domibusDate'
})

/**
 * This Pipe was created because Dates were not being correctly shown in IE/EDGE
 * It's an Angular4 known issue but they didn't solve yet.
 * When they solve it, we can delete this file and use the pipe with date and format again.
 */
export class DatePipe implements PipeTransform {
  transform(value: string, type: string = ""): string {
    if(value) {
      let d = new Date(value);

      let dd = ("0"+d.getDate()).slice(-2); //day
      let MM = ("0"+(d.getMonth()+1)).slice(-2); //month
      let yyyy = d.getFullYear(); //year
      let date = `${dd}-${MM}-${yyyy}`;

      let time = '';

      if (type != 'short') {
        let hh = ("0"+d.getHours()).slice(-2); //hours
        let mm = ("0"+d.getMinutes()).slice(-2); //minutes
        let ss = ("0"+d.getSeconds()).slice(-2); //seconds
        let currentTimezone = (d.getTimezoneOffset()/60) * -1;
        let gmt = 'GMT';
        if (currentTimezone !== 0) {
          gmt += currentTimezone > 0 ? '+' : ' ';
          gmt += currentTimezone;
        }
        time = `${hh}:${mm}:${ss}${gmt}`;
      }

      return `${date} ${time}`;
    }
    return "";
  }
}
