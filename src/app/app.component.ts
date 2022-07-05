import {Component} from '@angular/core';
import {map, Observable, of, startWith} from "rxjs";
import {AppState} from "./app-state";
import {Response} from "./response";
import {Controller} from "./controller";
import {DataState} from "./data-state.enum";
import {catchError} from "rxjs/operators";
import {Thing} from "./thing";
import {Ship} from "./ship";
import {Job} from "./job";
import {Status} from "./status.enum";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = "Seaport Simulation";
  component : string; // false = data / true = simulation
  readonly dataState = DataState;

  // Observers
  private data$ = this.controller.load$();
  public startResponse$: Observable<AppState<Response>>;
  private downloadResponse$ = this.controller.download$();
  public status$: Observable<Response>;
  public processing : boolean = true;
  public continue : boolean = true;
  public goFlag : boolean = true;
  public file : string = "";

  // Subscribers
  // So apparently this data map subscriber is not working properly as a Map because it was not initialized with a
  // map constructor ??? -.-
  // Because of that, it cannot iterate or execute its methods like get, push, filter etc.
  public dataSubscriber : Map<number, any>;


  // Data Structrues
  public data : any[] = [];
  public entries : any[] = [];
  public status : Map<string, Ship>;

  public ships: Ship[] = [];
  public ports: Thing[] = [];
  public selectedPort: string;

  // UI
  public startButton : string = "Start";
  public warning : string = "LOADING...";
  public safeToStart : boolean = false;
  public safeToCancel : boolean = true;


  constructor(private controller: Controller) {
    this.startResponse$ = new Observable<AppState<Response>>();
    //this.downloadResponse$ = new Observable<Response>();
    this.status$ = new Observable<Response>();

    this.status = new Map<string, Ship>();
    this.selectedPort = "";
    this.dataSubscriber = new Map<number, any>();

    //this.data = new Map<string, any>();
    //this.data = new Map(Object.entries(this.dataSubscriber));

    this.component = 'description';

    // Unfortunately, this does not work.
    //this.data$.subscribe(response => this.datatest = new Map(Object.entries(response.data.map)) );

    // @ts-ignore
    this.data$.subscribe(response => this.dataSubscriber = response.data.map );
    // @ts-ignore
    this.downloadResponse$.subscribe(response => this.file = response.data.result);

    // But I only want to execute this once, unless I reload the page
    this.contructMore();

  }

  // The following function is asynchronous, because the dataSubscriber does not load immediately
  public async contructMore() : Promise<void> {
    /*function delay(ms: number) {
      return new Promise( resolve => setTimeout(resolve, ms) );
    }*/
    await this.delay(1);
    // this.dataSubscriber is a map that subscribes to the HashMap received from the server
    // It does not work properly. It does not filter results, nor can I extract a set of keys or values from it.
    // In case I want to use a Map as a data structure in this case, I have to create a copy of this map using
    // the constructor of the Map class with the word new; eg: new Map().
    // This has to be executed after the promise/delay
    // When this new map is created from the constructor, the indexes are converted from numbers to strings -.-'
    // Eg.:
    // let data = new Map(Object.entries(this.dataSubscriber)); //Notice: .entries as opposed to .values
    // this.entries = Array.from(data.values());
    // console.log("entries count: ", this.entries.length);

    // I gave up on using Maps and decided to use an array.
    // Fortunately, I don't have to create another copy of the map, as I was able to retrieve the values with
    // Object.values()
    this.entries = Array.from(Object.values(this.dataSubscriber));
    this.data = this.entries;
    console.log("data count: ", this.data.length);

    // Since data map comes from the server in numerical order, it is safe to assign ports, then ships,
    // then jobs sequentially in this loop
    this.entries.forEach( (value: any) => {
      if (value.index >= 10000 && value.index < 20000) {
        this.ports.push(value);
      }
      else if (value.index >= 30000 && value.index < 50000) {
        value.jobs = [];
        let ship : Ship;
        ship = value;
        if (value.dock === "Any: ") {
          ship.port = this.entries.find( item => item.index == value.parent ).name;
        }
        else {
          let dock = this.entries.find( item => item.index == value.parent );
          ship.port = this.entries.find( item => item.index == dock.parent ).name;
        }
        this.ships.push(ship);
      }
      else if (value.index >= 60000) {
        // This local ship is a reference to the value in the map, no need to push an update back into it.
        //let ship = this.data.get(value.parent.toString());
        let ship : Ship;
        // @ts-ignore
        ship = this.ships.find(element => element.index == value.parent);
        ship.jobs.push(value as Job);
      }
    });


    this.ports.sort((a,b) => (a.index < b.index) ? 1 : -1);
    this.ships.sort((a,b) => (a.parent < b.parent) ? 1 : -1);

    this.safeToStart = true;
    this.warning = " ";

  }

  public showData() : void {
    this.component = 'data';
  }

  public async showSimulation() : Promise <void> {
    // First I gotta reload the DOM by enabling the simulation component.
    this.component = 'simulation';
    // Then I gotta add a delay to call the function and change the elements
    await this.delay(1);
    this.setSelected(this.ports[0].name);
  }

  public showDescription() : void {
    this.component = 'description';
  }

  public async showSource() : Promise <void> {
    this.component = 'source';
    await this.delay(1);
    const output = document.getElementById('output');
    // @ts-ignore
    output.innerHTML = this.file;
  }

  public showCode() : void {
    this.component = 'code';
  }

  // Yay, it's working, thanks StackOverflow!
  // Change which ports to display during simulation
  public setSelected(name: string) : void {
    this.ports.forEach(value => {
      const port = document.getElementById(value.name);
      //if (name === "any" || name === value.name) { In case I want to show all ports at the same time.
      if (name === value.name) {
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
    this.safeToStart = false;
    if (this.startButton === "Reload") {
      location.reload();
      await this.delay(1);
      if (typeof document.getElementById('cancelall') != null) {
        document.getElementById('cancelall')!.style.backgroundColor = 'white';
      }
      this.component = 'simulation';
    }
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

    let start = this.startResponse$.subscribe();
    while( this.processing && this.continue) {
      if (this.goFlag) {
        this.status$ = this.controller.getStatus$();
        // Creating a new map for every update is still very burdensome
        // @ts-ignore
        this.status$.subscribe(response => this.status = new Map(Object.entries(response.data.ships)) );
        await this.delay(1);

        this.getStatus();
      }
      await this.delay(400);
    }
    this.safeToStart = true;
    this.startButton = "Reload";
    console.log("This is the end!");
  }

  public getStatus() : void {
    this.processing = false;
    for (let ship of this.ships) {
      let mapShip = this.status.get(ship.index.toString());
      if (mapShip!.shipStatus.toString() != "DEPARTED") {
        this.processing = true;
      }
      ship.shipStatus = mapShip!.shipStatus;
      ship.jobs = mapShip!.jobs;
    }
  }

  public cancel(job: Job, shipStatus: Status) : void {
    if (shipStatus.toString() != "DEPARTED" && job.jobStatus.toString() != "FINISHED" &&
      job.jobStatus.toString() != "UNAVAILABLE") {
      this.controller.cancel$(job.index).subscribe();
    }
  }

  public pause(job: Job, shipStatus: Status) : void {
    if (shipStatus.toString() != "DEPARTED" && job.jobStatus.toString() != "FINISHED" &&
      job.jobStatus.toString() != "UNAVAILABLE") {
      this.controller.pause$(job.index).subscribe();
    }
  }

  public async cancelAll() : Promise<void> {
    this.continue = false;
    this.warning = "CANCELED ALL JOBS";
    const elem = document.getElementById('warning');
    elem!.style.color = 'red';
    document.getElementById('cancelall')!.style.backgroundColor = 'red';
    // Call to server to cancel and reset
    this.controller.cancelAll$().subscribe();

  }

  public async pauseAll() : Promise<void> {
    const elem = document.getElementById('warning');
    const button = document.getElementById('pauseall');
    if (this.goFlag == true) {
      this.goFlag = false;
      this.warning = "PAUSED ALL JOBS";
      elem!.style.color = 'orange';
      button!.style.backgroundColor = 'orange';
      // Call to server to cancel and reset
      this.controller.pauseAll$().subscribe();
    }
    else {
      this.goFlag = true;
      this.warning = " ";
      elem!.style.color = 'black';
      button!.style.backgroundColor = 'white';
      // Call to server to cancel and reset
      this.controller.pauseAll$().subscribe();
    }
  }

  public filter( event: any ) {
    // @ts-ignore
    let rows = document.getElementById('everything');
    let val = event.target.value.replace(/ +/g, ' ').toLowerCase();
    // @ts-ignore
    rows.show().filter( function() {
      let text = event.target.value.replace(/\s+/g, ' ').toLowerCase();
      return !~text.indexOf(val);
    })
  }

  public term = '';
  public search(term: string) : void {
    if (/^\d+$/.test(term)) {
      this.data = this.entries.filter((value) => value.index.toString().includes(term) ||
        value.parent.toString().includes(term));
    }
    else
      this.data = this.entries.filter((value) => value.name.replace(/\s+/g, ' ').toLowerCase().includes(term.toLowerCase()));
  }

  public sort( option: number ) : void {
    if (option == 0)
      this.data = this.entries.sort( (a,b) => (a.name > b.name) ? 1 : -1 );
    else if (option == 1)
      this.data = this.entries.sort( (a,b) => (a.index > b.index) ? 1 : -1 );
    else
      this.data = this.entries.sort( (a,b) => (a.parent > b.parent) ? 1 : -1 );
  }
}

// Before overhaul

/*
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

  public getSubject() : void {
    this.subject$ = this.controller.status$();
    // @ts-ignore
    this.subject$.subscribe(response => this.status = response.data.result );

    if (this.status === "Waiting") {console.log("Status: Waiting");}
    if (this.status === "Processing") {console.log("Status: Processing");}
    if (this.status === "Completed") {console.log("Status: Completed");}
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

// FOR loop for maps
/*for (const [key, value] of this.status) {
  if (value.shipStatus != Status.DEPARTED) {
    this.processing = true;
    break;
  }
}*/
// How to access map value attributes:
//console.log(this.data.get("20000").name);
