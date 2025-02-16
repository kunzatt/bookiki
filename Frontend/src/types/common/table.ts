// export interface TableColumn {
//   key: string;
//   label: string;
//   width?: string;
//   align?: 'left' | 'center' | 'right';
// }

export interface TableColumn {
  key: string;
  label: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  render?: (row: any) => {
    component: any;
    props: Record<string, any>;
  };
}
