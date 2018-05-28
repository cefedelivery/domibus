export interface DirtyOperations {
  isDirty(): boolean;
  undoChanges(): void;
  clearDirty(): void;
}
