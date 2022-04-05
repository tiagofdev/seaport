import { DataState } from "./data-state.enum";

export interface AppState<T> {
  dataState: DataState;
  response?: T;
  error?: string;
}
