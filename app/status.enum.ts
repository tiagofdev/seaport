export enum Status {
  ASSIGNING,
  DOCKING,
  DOCKED,
  DEPARTED,
  WAITING,
  PROCESSING,
  PAUSED,
  UNAVAILABLE,
  CANCELED,
  FINISHED
  // Ship                         Color       Job
  // Docking = Waiting to dock - Yellow       Waiting
  // Assigning = Assigning dock - Orange      Paused
  // Docked = Docked            - Green       Processing
  // Gone = ship is gone          - Blue      Finished
  //                              - Red       Canceled or Unavailable
}
