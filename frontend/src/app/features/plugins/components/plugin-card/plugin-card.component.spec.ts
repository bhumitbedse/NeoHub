import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PluginCardComponent } from './plugin-card.component';

describe('PluginCardComponent', () => {
  let component: PluginCardComponent;
  let fixture: ComponentFixture<PluginCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PluginCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PluginCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
