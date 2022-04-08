import {Component} from '@angular/core';
import {NgForm} from "@angular/forms";
import {BehaviorSubject, map, Observable, Observer, of, startWith} from "rxjs";
import {AppState} from "./app-state";
import {Response} from "./response";
import {Controller} from "./controller";
import {DataState} from "./data-state.enum";
import {catchError} from "rxjs/operators";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = "Seaport Simulation";
  readonly dataState = DataState;

  constructor(private controller: Controller) {
    this.response$ = new Observable<AppState<Response>>();
    this.status$ = new Observable<AppState<Response>>();
    this.subject$ = new Observable<Response>();
    this.status = "";
    this.counter = 0;
  }
  response$: Observable<AppState<Response>>;
  status$: Observable<AppState<Response>>;
  subject$: Observable<Response>;
  progress$!: Observable<any>;
  public response : any;
  public status : string;
  public counter : number;

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
    await this.delay(1);
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

  public getSubject() : void {
    this.subject$ = this.controller.status$();
    this.subject$.subscribe(response => this.status = response.data.result );

    if (this.status === "Waiting") {console.log("Status: Waiting");}
    if (this.status === "Processing") {console.log("Status: Processing");}
    if (this.status === "Completed") {console.log("Status: Completed");}
  }

  public async process(): Promise<void> {
    this.status = "";
    this.start();
    this.response = this.response$.subscribe(); // This is necessary, don't know why

    function delay(ms: number) {
      return new Promise( resolve => setTimeout(resolve, ms) );
    }
    this.counter = 0;
    this.getSubject();
    while( this.status != "Completed" && !this.cancelled ) {
      await delay(1000);
      this.counter++;
      this.getSubject();
    }


  }

  public cancelled = false;
  public stop() : void {
    this.cancelled = true;
    this.status = "Cancelled";
    this.response.unsubscribe();

  }



}
