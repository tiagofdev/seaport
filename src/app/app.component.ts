import {Component} from '@angular/core';
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

  // Observers
  private data$ = this.controller.load$();
  public startResponse$: Observable<AppState<Response>>;
  public status$: Observable<AppState<Response>>;
  public safeToStart : boolean = false;

  // Subscribers
  // So apparently this data map subscriber is not working properly as a Map because it was not initialized with a
  // map constructor ??? -.-
  // Because of that, it cannot iterate or execute its methods like get, push, filter etc.
  public dataSubscriber : Map<number, any>;
  public status : string;

  // Data Structrues
  public data : Map<string, any>;
  public ships: Ship[] = [];
  public ports: Thing[] = [];
  public jobs: Job[] = [];
  public selectedPort: string;

  constructor(private controller: Controller) {
    this.startResponse$ = new Observable<AppState<Response>>();
    this.status$ = new Observable<AppState<Response>>();

    this.status = "";
    this.selectedPort = "";
    this.dataSubscriber = new Map<number, any>();
    this.data = new Map<string, any>();
    //this.data = new Map(Object.entries(this.dataSubscriber));
    this.component = true;

    // Unfortunately, this does not work.
    //this.data$.subscribe(response => this.datatest = new Map(Object.entries(response.data.map)) );

    // @ts-ignore
    this.data$.subscribe(response => this.dataSubscriber = response.data.map );

    // But I only want to execute this once, unless I reload the page
    this.contructMore();

  }

  // The following function is asynchronous, because the dataSubscriber does not load immediately
  public async contructMore() : Promise<void> {
    /*function delay(ms: number) {
      return new Promise( resolve => setTimeout(resolve, ms) );
    }*/
    await this.delay(1000);

    // I have to create a copy of the map in order to filter its results and this does not work in the constructor
    // This has to be executed after the promise/delay
    // When this new map is created from the constructor, the indexes are converted from numbers to strings -.-'
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
        // This local ship is a reference to the value in the map, no need to push an update back into it.
        let ship = this.data.get(value.parent.toString());
        ship.jobs.push(value as Job);
        this.jobs.push(value as Job);
      }
    });

    this.ports.sort((a,b) => (a.index < b.index) ? 1 : -1);
    this.ships.sort((a,b) => (a.parent < b.parent) ? 1 : -1);


    //await this.delay(1000);
    this.safeToStart = true;
    const elem = document.getElementById('warning');
    elem!.style.display = 'none';
    //this.setPort();
  }

  public showData() : void {
    this.component = false;
  }
  public showSimulation() : void {
    this.component = true;

  }

  // Yay, it's working, thanks StackOverflow!
  // Change which ports to display during simulation
  public setSelected(name: string) : void {
    this.ports.forEach(value => {
      const port = document.getElementById(value.name);
      if (name === "any" || name === value.name) {
        // @ts-ignore
        port.style.display = "block";
      }
      else {
        // @ts-ignore
        port.style.display = "none";
      }
    });
  }

  public delay(ms: number) {
    return new Promise( resolve => setTimeout(resolve, ms) );
  }

  // This function starts the simulation. Calls the server
  public async start(): Promise<void> {
    this.startResponse$ = this.controller.start$()
      .pipe(
        map(result => {
          return { dataState: DataState.LOADED_STATE, response: result }
        }),
        startWith({dataState: DataState.LOADING_STATE}),
        catchError((err: string) => {
          return of({ dataState: DataState.ERROR_STATE, error: err })
        })
      );


    let start : any;
    this.startResponse$.subscribe(result => start = result.response?.data.result);

  }









  public testFunction() : void {

    // How to access map value attributes:
    //console.log(this.data.get("20000").name);

  }

  /*
  // Tests


  subject$: Observable<Response>;
  progress$!: Observable<any>;
  // Tests
  public response : any;

  public counter : number;
*/

/*
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
  // **********************************************************************
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
    /!*this.progress$.subscribe({
      next(num) {
        console.log(num);
      },
      complete() {
        console.log('Finished sequence');
      }
    });*!/
  }
  */

}
