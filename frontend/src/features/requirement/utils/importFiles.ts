import type { ImportSourceType } from '../types/requirement';

export function isZipArchive(fileName: string | null | undefined): boolean {
  return Boolean(fileName && fileName.toLowerCase().endsWith('.zip'));
}

export function buildCombinedFileLabel(fileNames: ReadonlyArray<string>): string | null {
  if (fileNames.length === 0) return null;
  if (fileNames.length === 1) return fileNames[0];
  if (fileNames.length === 2) return `${fileNames[0]}, ${fileNames[1]}`;
  return `${fileNames[0]} + ${fileNames.length - 1} more`;
}

export function buildBatchUploadLabel(fileCount: number): string {
  return fileCount === 1 ? '1 file' : `${fileCount} files`;
}

export function describeImportedFile(file: File): string {
  const lowerName = file.name.toLowerCase();
  if (lowerName.endsWith('.zip')) return `[ZIP package upload: ${file.name}]`;
  if (lowerName.endsWith('.docx')) return `[Document upload: ${file.name}]`;
  if (
    lowerName.endsWith('.png') ||
    lowerName.endsWith('.jpg') ||
    lowerName.endsWith('.jpeg') ||
    lowerName.endsWith('.webp')
  ) {
    return `[Image upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.pdf')) return `[PDF upload: ${file.name}]`;
  if (lowerName.endsWith('.xlsx')) return `[Spreadsheet upload: ${file.name}]`;
  return `[File upload: ${file.name}]`;
}

export function describeImportedFiles(files: ReadonlyArray<File>): string {
  if (files.length === 0) return '';
  if (files.length === 1) return describeImportedFile(files[0]);
  return `[Batch upload: ${buildBatchUploadLabel(files.length)} including ${buildCombinedFileLabel(files.map(f => f.name))}]`;
}

export async function readImportedFile(file: File): Promise<string> {
  const lowerName = file.name.toLowerCase();
  if (
    lowerName.endsWith('.txt') ||
    lowerName.endsWith('.csv') ||
    lowerName.endsWith('.md') ||
    lowerName.endsWith('.html') ||
    lowerName.endsWith('.htm') ||
    lowerName.endsWith('.markdown') ||
    lowerName.endsWith('.json') ||
    lowerName.endsWith('.yaml') ||
    lowerName.endsWith('.yml') ||
    lowerName.endsWith('.xml') ||
    lowerName.endsWith('.eml') ||
    lowerName.endsWith('.msg') ||
    lowerName.endsWith('.vtt') ||
    file.type.startsWith('text/')
  ) {
    return file.text();
  }
  if (
    lowerName.endsWith('.png') ||
    lowerName.endsWith('.jpg') ||
    lowerName.endsWith('.jpeg') ||
    lowerName.endsWith('.webp')
  ) {
    return `[Image upload: ${file.name}]`;
  }
  if (lowerName.endsWith('.pdf')) return `[PDF upload: ${file.name}]`;
  if (lowerName.endsWith('.xlsx')) return `[Spreadsheet upload: ${file.name}]`;
  if (lowerName.endsWith('.docx')) return `[Document upload: ${file.name}]`;
  if (lowerName.endsWith('.zip')) return `[ZIP package upload: ${file.name}]`;
  return `[Binary upload: ${file.name}]`;
}

export function combineImportedText(
  files: ReadonlyArray<File>,
  contents: ReadonlyArray<string>,
): string {
  if (files.length === 0) return '';
  if (files.length === 1) return contents[0] ?? '';
  return files
    .map((file, i) => `Source file: ${file.name}\n${contents[i] ?? describeImportedFile(file)}`)
    .join('\n\n');
}

export function mapImportSourceType(sourceType: ImportSourceType): 'TEXT' | 'FILE' | 'EMAIL' | 'MEETING' {
  switch (sourceType) {
    case 'file':    return 'FILE';
    case 'email':   return 'EMAIL';
    case 'meeting': return 'MEETING';
    case 'paste':   return 'TEXT';
    default: {
      const _exhaustive: never = sourceType;
      return _exhaustive;
    }
  }
}
