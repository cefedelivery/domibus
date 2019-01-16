
export class DownloadService {

  public static downloadNative(content) {
    let element = document.createElement('a');
    element.setAttribute('href', content);
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }
}
