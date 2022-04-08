import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpEvent,
  HttpEventType,
  HttpProgressEvent,
  HttpResponse
} from "@angular/common/http";
import {Observable, throwError, Timestamp} from "rxjs";
import {tap, catchError} from "rxjs/operators";
import {Response} from "./response";

@Injectable({ providedIn: 'root' })
export class Controller {

  constructor(private http: HttpClient) { }

  // This is a procedural approach
  // getTwoSum() : Observable<CustomResponse> {
  //   return this.http.get<CustomResponse>('http://localhost:8080/twosum');
  // }

  private readonly  apiUrl = 'http://localhost:8080';

  // This is a reactive approach

  public status$ = () =>
    <Observable<Response>> this.http.get<Response>(`${this.apiUrl}/status`)
      .pipe(
        tap(console.log),
        catchError(Controller.handleError)
      );

  public start$ = () =>
    <Observable<Response>> this.http.get<Response>(`${this.apiUrl}/start`)
        .pipe(
          tap(console.log),
          catchError(Controller.handleError)
        );


  /*public start$ = (): Observable<Response> => {
    console.log("controler");
    return this.http.get<Response>(`${this.apiUrl}/start`)
      .pipe(
        tap(console.log),
        catchError(Controller.handleError)
      );
  }*/

  private static handleError(error: HttpErrorResponse): Observable<never> {
    console.log(error);
    return throwError(`Error occurred - Error Code: ${error.status}`);
  }
}
