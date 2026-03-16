package api

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
)

const defaultBase = "http://localhost:8080/api/v1"

type Client struct {
	BaseURL string
	http    *http.Client
}

func NewClient(baseURL string) *Client {
	if baseURL == "" {
		baseURL = defaultBase
	}
	return &Client{
		BaseURL: baseURL,
		http:    &http.Client{},
	}
}

type Plugin struct {
	ID          string   `json:"id"`
	Name        string   `json:"name"`
	Slug        string   `json:"slug"`
	FullName    string   `json:"fullName"`
	GithubOwner string   `json:"githubOwner"`
	GithubRepo  string   `json:"githubRepo"`
	Description string   `json:"description"`
	GithubURL   string   `json:"githubUrl"`
	GithubStars int      `json:"githubStars"`
	Tags        []string `json:"tags"`
	Category    string   `json:"categoryName"`
}

type PluginDetail struct {
	Plugin
	InstallGuide  string `json:"installGuide"`
	ConfigExample string `json:"configExample"`
	KeymapsSection string `json:"keymapsSection"`
}

type SearchResponse struct {
	Content       []Plugin `json:"content"`
	TotalElements int      `json:"totalElements"`
	TotalPages    int      `json:"totalPages"`
}

// SearchPlugins searches NeoHub for a plugin by name
func (c *Client) SearchPlugins(query string) ([]Plugin, error) {
	endpoint := fmt.Sprintf("%s/plugins/search?q=%s&size=5",
		c.BaseURL, url.QueryEscape(query))

	resp, err := c.http.Get(endpoint)
	if err != nil {
		return nil, fmt.Errorf("cannot reach NeoHub API: %w", err)
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	var result SearchResponse
	if err := json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	return result.Content, nil
}

// GetPlugin fetches full plugin details by slug
func (c *Client) GetPlugin(slug string) (*PluginDetail, error) {
	endpoint := fmt.Sprintf("%s/plugins/%s", c.BaseURL, slug)

	resp, err := c.http.Get(endpoint)
	if err != nil {
		return nil, fmt.Errorf("cannot reach NeoHub API: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode == 404 {
		return nil, fmt.Errorf("plugin '%s' not found on NeoHub", slug)
	}

	body, _ := io.ReadAll(resp.Body)
	var plugin PluginDetail
	if err := json.Unmarshal(body, &plugin); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	return &plugin, nil
}

// FindPlugin finds a plugin by name or slug — tries exact slug first then search
func (c *Client) FindPlugin(name string) (*PluginDetail, error) {
	// 1. Try direct slug lookup
	slug := strings.ToLower(strings.ReplaceAll(name, "/", "-"))
	slug = strings.ReplaceAll(slug, ".", "-")

	plugin, err := c.GetPlugin(slug)
	if err == nil {
		return plugin, nil
	}

	// 2. Try with -nvim suffix (common pattern)
	if !strings.HasSuffix(slug, "-nvim") {
		plugin, err = c.GetPlugin(slug + "-nvim")
		if err == nil {
			return plugin, nil
		}
	}

	// 3. Fall back to search with smart matching
	results, err := c.SearchPlugins(name)
	if err != nil || len(results) == 0 {
		return nil, fmt.Errorf("plugin '%s' not found", name)
	}

	search := strings.ToLower(name)
	bestMatch := results[0]
	bestScore := 0

	for _, r := range results {
		rName := strings.ToLower(r.Name)
		rSlug := strings.ToLower(r.Slug)
		score := 0

		// Exact name match wins
		if rName == search || rName == search+".nvim" {
			score = 100
		} else if rSlug == search || rSlug == search+"-nvim" {
			score = 90
		} else if strings.HasPrefix(rName, search+".") {
			score = 80
		} else if rName == search+".nvim" {
			score = 70
		} else if strings.HasPrefix(rSlug, search+"-") && !strings.Contains(rSlug[len(search):], "-") {
			// e.g. "telescope-nvim" for query "telescope" — only one suffix segment
			score = 60
		} else if strings.HasPrefix(rName, search) {
			score = 40
		}

		if score > bestScore {
			bestScore = score
			bestMatch = r
		}
	}

	return c.GetPlugin(bestMatch.Slug)
}