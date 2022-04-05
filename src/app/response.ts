export interface Response {
  timeStamp: Date;
  status: string;
  statusCode: number;
  message: string;
  data: { result: string };
}
