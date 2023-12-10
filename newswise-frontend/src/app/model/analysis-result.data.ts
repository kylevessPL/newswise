import {DiseaseRisk} from './disease-risk.enum';

export interface AnalysisResultData {
    disease: boolean;
    probability: number;
    risk: DiseaseRisk;
}
