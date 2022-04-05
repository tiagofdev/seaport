import {Component} from '@angular/core';
import {NgForm} from "@angular/forms";
import {async, BehaviorSubject, filter, first, map, Observable, Observer, of, startWith} from "rxjs";
import {AppState} from "./app-state";
import {Response} from "./response";
import {Controller} from "./controller";
import {DataState} from "./data-state.enum";
import {catchError} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = "Seaport Simulation";
  readonly dataState = DataState;

  constructor(private controller: Controller,
              private http: HttpClient) {
    this.response$ = new Observable<AppState<Response>>();
    this.status$ = new Observable<AppState<Response>>();
    this.subject$ = new Observable<Response>();
  }
  response$: Observable<AppState<Response>>;
  status$: Observable<AppState<Response>>;
  subject$: Observable<Response>;
  started = false;

  start(): void {
    this.response$ = this.controller.start$()
      .pipe(
        map(result => {
          return { dataState: DataState.LOADED_STATE, response: result }
        }),
        startWith({dataState: DataState.LOADING_STATE}),
        catchError((err: string) => {
          return of({ dataState: DataState.ERROR_STATE, error: err })
        })
      );

  }

  public delay(n: number){
    return new Promise(function(resolve){
      setTimeout(resolve,n*1000);
    });
  }


   async getStatus(): Promise<void> {
    await this.delay(4);

    this.status$ = this.controller.status$()
      .pipe(
        map(result => {
          return {dataState: DataState.LOADED_STATE, response: result}
        }),
        startWith({dataState: DataState.LOADING_STATE}),
        catchError((err: string) => {
          return of({dataState: DataState.ERROR_STATE, error: err})
        })
      );
  }

  getSubject() : void {
    console.log("subject: ", this.status)
    this.subject$ = this.controller.status$();

  }


  public sequenceSubscriber(observer: Observer<any>) {
    let timeoutId: any;
    // Will run through an array of numbers, emitting one value
    // per second until it gets to the end of the array.
    function doInSequence(idx: number) {

      timeoutId = setInterval(() => {
        observer.next(idx);
        if (idx === 5) {
          observer.complete();
        } else {
          doInSequence(++idx);
        }
      }, 1000);
    }

    doInSequence(0);
    // Unsubscribe should clear the timeout to stop execution
    return {
      unsubscribe() {
        clearTimeout(timeoutId);
      }
    };
  }

  // Create a new Observable that will deliver the above sequence
  // Create a new Observable that will deliver the above sequence
  progress$!: Observable<any>;

  public listen() : void {
    this.progress$ = new Observable(this.sequenceSubscriber);
    /*this.progress$.subscribe({
      next(num) {
        console.log(num);
      },
      complete() {
        console.log('Finished sequence');
      }
    });*/
  }
  public response : any;
  public status : any;
  public process() : void {
    this.started = true;

    this.start();
    this.response = this.response$.subscribe();
    console.log("response: ", this.response$);

    //this.getStatus().then(r => {});
    this.getSubject();

    this.subject$.subscribe(response => this.status = response.data.result);
    console.log("ffs: ", this.status);

    /*while (this.status != "Completed") {

      this.getStatus().then(r => {});
      console.log("Updating Status");
    }*/

  }

  public stop() : void {
    this.started = false;
    this.status = "Completed";
    this.response.unsubscribe();
    //this.status.unsubscribe();
  }

}
