import { DomibusAngular2WebPage } from './app.po';

describe('domibus-MSH-web App', function() {
  let page: DomibusAngular2WebPage;

  beforeEach(() => {
    page = new DomibusAngular2WebPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
