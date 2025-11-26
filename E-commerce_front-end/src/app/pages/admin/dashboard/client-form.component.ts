import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-client-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './client-form.component.html',
})
export class ClientFormComponent implements OnInit, OnChanges {
  @Input() client: any | null = null;
  @Output() save = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  clientForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.clientForm = this.fb.group({
      id: [null],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: [''], // Ajout du champ mot de passe
      registrationDate: ['']
    });
  }

  ngOnInit(): void {
    this.updateForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['client']) {
      this.updateForm();
    }
  }

  private updateForm(): void {
    if (this.client) {
      // Mode édition : pas besoin de mot de passe
      this.clientForm.get('password')?.clearValidators();
      this.clientForm.patchValue(this.client);
    } else {
      // Mode création : le mot de passe est requis
      this.clientForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.clientForm.reset({ registrationDate: new Date().toISOString().split('T')[0] });
    }
    this.clientForm.get('password')?.updateValueAndValidity();
  }

  onSave(): void {
    if (this.clientForm.valid) {
      this.save.emit(this.clientForm.value);
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }
}