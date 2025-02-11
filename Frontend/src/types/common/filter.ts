import { SelectOption } from "./select";

export interface FilterConfig {
    type: 'select' | 'date' | 'radio' | 'checkbox' | 'search';
    key: string;
    label: string;
    options?: SelectOption[];
    multiple?: boolean;
    placeholder?: string;
  }