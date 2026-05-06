/*
 * Copyright 2025-present The Concordile Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import js from '@eslint/js'
import prettier from 'eslint-config-prettier'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import importX from 'eslint-plugin-import-x'
import simpleImportSort from 'eslint-plugin-simple-import-sort'
import { defineConfig, globalIgnores } from 'eslint/config'
import globals from 'globals'
import tseslint from 'typescript-eslint'

export default defineConfig([
  globalIgnores([
    'dist',
    'src/shared/api/index.ts',
    'src/shared/api/generated/**',
  ]),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
      prettier,
    ],
    languageOptions: {
      globals: globals.browser,
    },
    plugins: {
      'import-x': importX,
      'simple-import-sort': simpleImportSort,
    },
    rules: {
      'no-restricted-imports': 'off',

      '@typescript-eslint/no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              regex: '\\.(ts|tsx|mts|cts|js|jsx|mjs|cjs)$',
              message:
                'Do not use file extensions in code imports. Use extensionless imports instead.',
            },
            {
              regex: '^\\.\\./',
              message:
                'Do not use parent-relative imports. Use the "@/..." alias instead.',
            },
          ],
        },
      ],
      'import-x/extensions': [
        'error',
        'never',
        {
          ignorePackages: true,
          pattern: {
            js: 'never',
            jsx: 'never',
            ts: 'never',
            tsx: 'never',
            mjs: 'never',
            cjs: 'never',
            mts: 'never',
            cts: 'never',
            css: 'always',
          },
        },
      ],
      'simple-import-sort/imports': [
        'error',
        {
          groups: [
            // React ecosystem
            [
              '^react(?:\\u0000)?$',
              '^react-dom(?:/.*)?(?:\\u0000)?$',
              '^react-router-dom(?:\\u0000)?$',
            ],
            // External libraries
            ['^@?\\w'],
            // Dashboard imports
            ['^@/'],
            // Relative imports
            ['^\\.'],
            // Side-effect imports, for example CSS
            ['^\\u0000'],
          ],
        },
      ],
      'simple-import-sort/exports': 'error',
    },
  },
])
