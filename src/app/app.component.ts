import {AfterViewInit, Component, OnInit} from '@angular/core';
import {NgForm} from "@angular/forms";
import {BehaviorSubject, map, Observable, Observer, of, startWith} from "rxjs";
import {AppState} from "./app-state";
import {Response} from "./response";
import {Controller} from "./controller";
import {DataState} from "./data-state.enum";
import {catchError} from "rxjs/operators";
import {Thing} from "./thing";
import {Ship} from "./ship";
import {Job} from "./job";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = "Seaport Simulation";
  component : boolean; // false = data / true = simulation
  readonly dataState = DataState;

  private data$ = this.controller.load$();
  public dataSubscriber : Map<number, any>;
  public data : Map<string, any>;
  public datatest : Map<number, any>;
  // So apparently this data map subscriber is not working properly as a Map because it was not initialized with a
  // map constructor ??? -.-
  // Because of that, it cannot iterate or execute its methods like get, push, filter etc.

  public ships: Ship[] = [];
  public ports: Thing[] = [];
  public jobs: Job[] = [];
  public selectedPort: string;

  constructor(private controller: Controller) {
    this.response$ = new Observable<AppState<Response>>();
    this.status$ = new Observable<AppState<Response>>();
    this.subject$ = new Observable<Response>();


    this.status = "";
    this.selectedPort = "";
    this.counter = 0;
    this.dataSubscriber = new Map<number, any>();
    this.data = new Map<string, any>();
    this.datatest = new Map<number, any>();
    //this.data$ = this.controller.load$();
    //this.data = new Map(Object.entries(this.dataSubscriber));
    this.component = false;
    // But I only want to execute this once, unless I reload the page

    // @ts-ignore
    this.data$.subscribe(response => this.dataSubscriber = response.data.map );
    this.contructMore();
    // @ts-ignore
    this.data$.subscribe(response => this.datatest = new Map(Object.entries(response.data.map)) );


  }

  // The following function is asynchronous, because the dataSubscriber does not load immediately
  public async contructMore() : Promise<void> {
    function delay(ms: number) {
      return new Promise( resolve => setTimeout(resolve, ms) );
    }
    await delay(1000);
    console.log("function");

    // I have to create a copy of the map in order to filter its results and this does not work in the constructor
    this.data = new Map(Object.entries(this.dataSubscriber));

    // Since data map comes from the server in numerical order, it is safe to assign ports, then ships,
    // then jobs sequentially in this loop
    this.data.forEach((value: any, key: string) => {
      let keyN : number = +key;
      if (keyN >= 10000 && keyN < 20000) {
        this.ports.push(value);
      }
      else if (keyN >= 30000 && keyN < 50000) {
        value.jobs = new Array();
        if (value.dock === "Any: ") {
          let index = value.parent;
          value.port = this.data.get(index.toString()).name;
        }
        else {
          let dock = value.parent;
          let port = this.data.get(dock.toString()).parent;
          value.port = this.data.get(port.toString()).name;
        }
        this.ships.push(value as Ship);
      }
      else if (keyN >= 60000) {
        let ship = this.data.get(value.parent.toString());
        ship.jobs.push(value as Job);
        //this.ships.push(ship as Ship);
        this.jobs.push(value as Job);
      }
    });

    this.ports.sort((a,b) => (a.index < b.index) ? 1 : -1);
    this.ships.sort((a,b) => (a.parent < b.parent) ? 1 : -1);
    //this.setPort();
  }

  public somefunction() : void {

    //console.log("get value: ", this.ships[1].jobs[0].name);
    // How to access map value attributes:
    //console.log(this.data.get("20000").name);

  }



  public showData() : void {
    this.component = false;
  }
  public showSimulation() : void {
    this.component = true;
  }







  public setSelected() : void {

  }


  // Tests
  status$: Observable<AppState<Response>>;
  response$: Observable<AppState<Response>>;
  subject$: Observable<Response>;
  progress$!: Observable<any>;
  // Tests
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
    // @ts-ignore
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
