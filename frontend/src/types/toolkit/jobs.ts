import {JobState} from '@/types/toolkit/enums';
import {AlignmentItem} from '@/types/toolkit/results';

export interface Job {
    jobID: string;
    status: JobState;
    tool: string;
    code: string;
    parentID?: string;
    watched: boolean;
    foreign: boolean;
    isPublic: boolean;
    dateCreated?: number;
    dateUpdated?: number;
    dateViewed?: number;
    paramValues?: { string: any };
    views?: JobViewOptions[];
}

export interface JobViewOptions {
    component: string;
    title?: string;
    // data tab options
    filename?: string;
}

export interface SubmissionResponse {
    successful: boolean;
    code: number;
    message: string;
    jobID: string;
}

export interface CustomJobIdValidationResult {
    exists: boolean;
    suggested?: string;
}

export interface SimilarJobResult {
    jobID: string;
    dateCreated: number;
}
