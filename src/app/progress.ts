export interface Progress {
  state: 'PENDING' | 'IN_PROGRESS' | 'DONE'
  progress: number
  content: Blob | null
}
